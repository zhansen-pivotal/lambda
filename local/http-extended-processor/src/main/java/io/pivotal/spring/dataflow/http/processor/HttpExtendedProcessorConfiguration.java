package io.pivotal.spring.dataflow.http.processor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@EnableBinding(Processor.class)
public class HttpExtendedProcessorConfiguration {

	private static Logger LOG = LoggerFactory.getLogger(HttpExtendedProcessorConfiguration.class);

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object httpGet(Object payload) throws Exception {
		String line, lat, lon, jsonString, mString;
		Map<String, Object> m = new HashMap<String, Object>();

		StringBuilder result = new StringBuilder();
		String uuid = UUID.randomUUID().toString();
		ObjectMapper mapper = new ObjectMapper();

		LOG.info("Payload:{}", payload);
		try {
			URL url = new URL(payload.toString());

			String[] parsedUrl = payload.toString().split("&");
			lat = parsedUrl[1].split("=")[1];
			lon = parsedUrl[2].split("=")[1];

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				LOG.info("The Line={}", line);
				result.append(line);
			}

			JSONObject json = new JSONObject(result.toString());
			jsonString = json.getJSONObject("Results").toString();

			m = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
			m.put("lat", lat);
			m.put("lon", lon);
			m.put("id", uuid);
			

			rd.close();
		} catch (Exception e) {
			throw new Exception();
		}

		LOG.info("Returned={}", jsonString);

		return m;
	}
}
