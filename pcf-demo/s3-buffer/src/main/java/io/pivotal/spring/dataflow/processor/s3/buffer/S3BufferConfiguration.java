package io.pivotal.spring.dataflow.processor.s3.buffer;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.aggregator.ExpressionEvaluatingCorrelationStrategy;
import org.springframework.integration.aggregator.MessageCountReleaseStrategy;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.AggregatorFactoryBean;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@EnableConfigurationProperties(S3BufferProperties.class)
@EnableBinding(Processor.class)
public class S3BufferConfiguration {
	private static Logger LOG = LoggerFactory.getLogger(S3BufferConfiguration.class);

	private MessageChannel output;
	@Autowired
	S3BufferProperties property;
	
	@Autowired
	public void SendingBean(MessageChannel output) {
		this.output = output;
	}

	@Bean
    @Primary
    @ServiceActivator(inputChannel= Processor.INPUT)
	FactoryBean<MessageHandler> aggregatorFactoryBean(MessageChannel toSink, MessageGroupStore messageGroupStore) {
		AggregatorFactoryBean aggregatorFactoryBean = new AggregatorFactoryBean();
		aggregatorFactoryBean
				.setCorrelationStrategy(new ExpressionEvaluatingCorrelationStrategy("payload.getClass().name"));
		aggregatorFactoryBean.setReleaseStrategy(new MessageCountReleaseStrategy(property.getAggSize()));
		aggregatorFactoryBean.setMessageStore(messageGroupStore);
		aggregatorFactoryBean.setProcessorBean(new DefaultAggregatingMessageGroupProcessor());
		aggregatorFactoryBean.setExpireGroupsUponCompletion(true);
		aggregatorFactoryBean.setSendPartialResultOnExpiry(true);
		aggregatorFactoryBean.setOutputChannel(toSink);
		return aggregatorFactoryBean;
	}

    @Bean
    @ServiceActivator(inputChannel = "toSink")
	public MessageHandler datasetSinkMessageHandler() {
		return new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				Object payload = message.getPayload();
				if (payload instanceof Collection<?>) {
					Collection<?> payloads = (Collection<?>) payload;
					LOG.debug("Writing a collection of {} POJOs" + payloads.size());
					output.send((Message<?>) message.getPayload());
				} else {
					// This should never happen since message handler is fronted
					// by an aggregator
					throw new IllegalStateException(
							"Expected a collection of POJOs but received " + message.getPayload().getClass().getName());
				}
			}
		};
	}
}
