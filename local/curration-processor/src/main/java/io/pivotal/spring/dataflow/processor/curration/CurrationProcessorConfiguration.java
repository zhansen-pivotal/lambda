package io.pivotal.spring.dataflow.processor.curration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;

import com.fasterxml.jackson.databind.ObjectMapper;

@EnableConfigurationProperties(CurrationProcessorProperties.class)
@EnableBinding(Processor.class)
public class CurrationProcessorConfiguration {
	private static Logger LOG = LoggerFactory.getLogger(CurrationProcessorConfiguration.class);
	
	@Autowired
	CurrationProcessorProperties props;
	
	@SuppressWarnings("unchecked")
	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public String cleanData(Object payload) throws Exception {
		//String d = payload.toString();
		LOG.debug("Payload = {} , Payload class={}", payload.toString() , payload.getClass());
		
		@SuppressWarnings("rawtypes")
		Map json = (Map) payload;
		LOG.debug("JSONObject = {}", json);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = Calendar.getInstance().getTime();
		String sDate = dateFormat.format(date);
		
		///LOG.info("Date = {}", sDate);
		
		json.put("Date", sDate);
		json.put("Source", this.props.getDataSource());
		json.put("Destination", this.props.getDestination());
		json.put("Group", this.props.getSecurityGroup());
		json.put("DataSetYear", this.props.getDataSetYear());
		
		
		
		String mapAsJson = new ObjectMapper().writeValueAsString(json);
		JSONObject v = new JSONObject(mapAsJson);
		LOG.info("To return : {}", v.toString());
		String retVal = v.toString();
		return retVal;
	}
}
