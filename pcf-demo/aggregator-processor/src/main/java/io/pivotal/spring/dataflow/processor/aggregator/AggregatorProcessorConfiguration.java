package io.pivotal.spring.dataflow.processor.aggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;

@EnableBinding(Processor.class)
public class AggregatorProcessorConfiguration {

	private static Logger LOG = LoggerFactory.getLogger(AggregatorProcessorConfiguration.class);

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public String cleanData(Object payload) throws Exception {
		LOG.info("Payload = {} , Payload class={}", payload.toString(), payload.getClass());
		StringBuilder builder = new StringBuilder();
		while (builder.length() < 10) {
			builder.append(payload);
		}
		String message = builder.toString();
		return message;
	}

}
