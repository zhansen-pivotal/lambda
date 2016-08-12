package io.pivotal.spring.dataflow.processor.curration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class CurrationProcessorProperties {

	@Value("${dataSource:unknown}")
	public String dataSource;

	@Value("${destination:unknown}")
	public String destination;

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
}
