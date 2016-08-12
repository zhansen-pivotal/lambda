package io.pivotal.spring.dataflow.processor.curration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;

@EnableBinding(Processor.class)
public class CurrationProcessorConfiguration {
	private static Logger LOG = LoggerFactory.getLogger(CurrationProcessorConfiguration.class);

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public String cleanData(Object payload) throws Exception {
		String d = payload.toString();
		LOG.info("Payload = {} , Payload class={}", payload.toString() , payload.getClass());
		
		JSONObject json = new JSONObject(d);
		LOG.info("JSONObject = {}", json);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = Calendar.getInstance().getTime();
		String sDate = dateFormat.format(date);
		
		LOG.info("Date = {}", sDate);
		
		json.put("Date", sDate);
		json.put("Source", "www.broadbandmap.gov/broadbandmap/demographic/2014/coordinates");
		json.put("Destination","Gemfire");
		
		
		String retVal = json.toString();
		return retVal;
	}
}
