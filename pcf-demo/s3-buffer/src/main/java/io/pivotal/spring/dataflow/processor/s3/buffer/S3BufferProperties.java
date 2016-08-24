package io.pivotal.spring.dataflow.processor.s3.buffer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class S3BufferProperties {
	
	@Value("${aggSize:100}")
	public int aggSize;

	public int getAggSize() {
		return aggSize;
	}

	public void setAggSize(int aggSize) {
		this.aggSize = aggSize;
	}	
}
