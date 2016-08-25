package io.pivotal.spring.dataflow.processor.s3.buffer;

import java.util.Collection;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.InputBindingLifecycle;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.aggregator.ExpressionEvaluatingCorrelationStrategy;
import org.springframework.integration.aggregator.MessageCountReleaseStrategy;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.AggregatorFactoryBean;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.MessageGroupStoreReaper;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Scheduled;

@EnableBinding(Processor.class)
public class S3BufferConfiguration {
	private static Logger LOG = LoggerFactory.getLogger(S3BufferConfiguration.class);

	@Bean
	public MessageChannel toSink() {
		return new DirectChannel();
	}
	
	private MessageChannel output;

	@Autowired
	public void SendingBean(MessageChannel output) {
		this.output = output;
	}

	@Bean
	@Primary
	@ServiceActivator(inputChannel = Processor.INPUT)
	FactoryBean<MessageHandler> aggregatorFactoryBean(MessageChannel toSink, MessageGroupStore messageGroupStore) {
		LOG.info("Starting Aggregator");
		AggregatorFactoryBean aggregatorFactoryBean = new AggregatorFactoryBean();
		aggregatorFactoryBean
				.setCorrelationStrategy(new ExpressionEvaluatingCorrelationStrategy("payload.getClass().name"));
		aggregatorFactoryBean.setReleaseStrategy(new MessageCountReleaseStrategy(10));
		aggregatorFactoryBean.setMessageStore(messageGroupStore);
		aggregatorFactoryBean.setProcessorBean(new DefaultAggregatingMessageGroupProcessor());
		aggregatorFactoryBean.setExpireGroupsUponCompletion(true);
		aggregatorFactoryBean.setSendPartialResultOnExpiry(true);
		aggregatorFactoryBean.setOutputChannel(toSink);
		LOG.info("Aggregator about to return aggreFactoryBean");
		return aggregatorFactoryBean;
	}

	@Bean
	@ServiceActivator(inputChannel = "toSink")
//	public MessageHandler datasetSinkMessageHandler() {
//		return new MessageHandler() {
//
//			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				Object payload = message.getPayload();
				if (payload instanceof Collection<?>) {
					Collection<?> payloads = (Collection<?>) payload;
					LOG.info("Writing a collection of {} POJOs" + " " + payload.toString() + payloads.size());
					output.send((Message<?>) message.getPayload());
				} else {
					// This should never happen since message handler is fronted
					// by an aggregator
					throw new IllegalStateException(
							"Expected a collection of POJOs but received " + message.getPayload().getClass().getName());
				}
			}
		//}

	//}

	@Bean
	MessageGroupStore messageGroupStore() {
		SimpleMessageStore messageGroupStore = new SimpleMessageStore();
		messageGroupStore.setTimeoutOnIdle(true);
		messageGroupStore.setCopyOnGet(false);
		return messageGroupStore;
	}

	@Bean
	MessageGroupStoreReaper messageGroupStoreReaper(MessageGroupStore messageStore,
			InputBindingLifecycle inputBindingLifecycle) {
		MessageGroupStoreReaper messageGroupStoreReaper = new MessageGroupStoreReaper(messageStore);
		messageGroupStoreReaper.setPhase(inputBindingLifecycle.getPhase() - 1);
		// messageGroupStoreReaper.setTimeout(properties.getIdleTimeout());
		messageGroupStoreReaper.setAutoStartup(true);
		messageGroupStoreReaper.setExpireOnDestroy(true);
		return messageGroupStoreReaper;
	}

	@Bean
	ReaperTask reaperTask() {
		return new ReaperTask();
	}

	public static class ReaperTask {

		@Autowired
		MessageGroupStoreReaper messageGroupStoreReaper;

		@Scheduled(fixedRate = 1000)
		public void reap() {
			messageGroupStoreReaper.run();
		}

		@PreDestroy
		public void beforeDestroy() {
			reap();
		}

	}
}
