package io.pivotal.spring.dataflow.processor.hdfs.formatter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;

@EnableBinding(Processor.class)
public class HdfsFormatterConfiguration {

	private static Logger LOG = LoggerFactory.getLogger(HdfsFormatterConfiguration.class);

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object doFormat(Object payload) throws Exception {
		LOG.debug("Payload = {} , Payload class={}", payload.toString(), payload.getClass());

		JSONObject jObj = new JSONObject(payload.toString());
		LOG.debug("JSON to Transform={}", jObj.toString());

		String jsonAsCsv = jObj.getString("DataSetYear") + "," + jObj.get("Group") + "," + "HDFS" + "," + jObj.get("blockFips") + "," + jObj.get("Source") + ","
				+ jObj.get("id") + "," + jObj.get("Date") + "," + jObj.get("lon") + "," + jObj.get("lat") + ","
				+ jObj.get("incomeBetween100to200") + "," + jObj.get("incomeLessThan25") + ","
				+ jObj.get("incomeBetween25to50") + "," + jObj.get("incomeBetween50to100") + ","
				+ jObj.get("medianIncome");
	
		LOG.debug("Transformed Payload as CSV = {}", jsonAsCsv);

		return jsonAsCsv;

	}
}
