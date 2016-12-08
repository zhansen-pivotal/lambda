package io.pivotal.spring.dataflow.processor.curration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("demo.properties")
public class CurrationProcessorProperties {

	@Value("${dataSource:unknown}")
	public String dataSource;

	@Value("${destination:unknown}")
	public String destination;
	
	@Value("${securityGroup:unknown}")
	public String securityGroup;
	
	@Value("${dataSetYear:unknown}")
	public String dataSetYear;

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

	public String getSecurityGroup() {
		return securityGroup;
	}

	public void setSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup;
	}

	public String getDataSetYear() {
		return dataSetYear;
	}

	public void setDataSetYear(String dataSetYear) {
		this.dataSetYear = dataSetYear;
	}
}
