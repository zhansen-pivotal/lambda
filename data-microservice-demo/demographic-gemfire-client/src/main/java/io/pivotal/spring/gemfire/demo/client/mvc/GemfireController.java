package io.pivotal.spring.gemfire.demo.client.mvc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.Struct;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.gemstone.org.json.JSONArray;
import com.gemstone.org.json.JSONObject;

@RestController
public class GemfireController {
	private static final Logger LOG = LoggerFactory.getLogger(GemfireController.class);

	@Autowired
	ClientCache cache;

	@RequestMapping(value = "/initMap")
	public @ResponseBody String initMap() throws Exception {
		LOG.info("Building Map of Lats and Lons...");

		String queryString = "SELECT e.value.lat, e.value.lon from /demo.entrySet e";
		JSONArray latsAndLons = new JSONArray();

		QueryService queryService = cache.getQueryService();
		Query query = queryService.newQuery(queryString);
		SelectResults results = (SelectResults) query.execute();

		if (results.isEmpty()) {
			LOG.info("Query Result is Empty");
			throw new Exception();
		} else

			for (Iterator iter = results.iterator(); iter.hasNext();) {
				Struct location = (Struct) iter.next();
				JSONObject json = new JSONObject();
				json.put("lat", location.get("lat"));
				json.put("lon", location.get("lon"));
				latsAndLons.put(json);
			}

		String j = latsAndLons.toString();
		return j;
	}

	@RequestMapping(value = "/allDemographics")
	public @ResponseBody String queryDemographics() throws Exception {
		LOG.info("Query on /allDemographics");

		String queryString = "SELECT * from /demo";
		JSONArray ret = new JSONArray();

		QueryService queryService = cache.getQueryService();
		Query query = queryService.newQuery(queryString);
		SelectResults results = (SelectResults) query.execute();

		if (results.isEmpty()) {
			LOG.info("Query Result is Empty");
			throw new Exception();
		} else

			for (Iterator iter = results.iterator(); iter.hasNext();) {
				PdxInstance pdx = (PdxInstance) iter.next();
				String jsonString = JSONFormatter.toJSON(pdx);
				JSONObject json = new JSONObject(jsonString);
				ret.put(json);
			}

		String j = ret.toString();
		return j;
	}

	@RequestMapping(value = "/getByLatLon")
	public @ResponseBody String queryData(String lat, String lon) throws Exception {

		JSONArray ret = new JSONArray();
		String queryString = "SELECT e.value FROM /demo.entrySet e WHERE e.value.lat = " + lat + " AND e.value.lon ="
				+ lon;

		LOG.info("QueryParams: lat={}, lon={}", lat, lon);

		QueryService queryService = cache.getQueryService();
		Query query = queryService.newQuery(queryString);

		SelectResults results = (SelectResults) query.execute();

		if (results.isEmpty()) {
			LOG.error("Query Result is Empty");
		} else

			for (Iterator iter = results.iterator(); iter.hasNext();) {
				PdxInstance pdx = (PdxInstance) iter.next();
				String json = JSONFormatter.toJSON(pdx);
				JSONObject obj = new JSONObject(json);
				ret.put(obj);
			}
		String j = ret.toString();
		return j;
	}

	@RequestMapping(value = "/getBounding")
	public @ResponseBody String queryBox(String lat, String lon) throws Exception {
		JSONArray f = new JSONArray();
		Long v = getFips(lat, lon);
		LOG.info("fipsCode={}", v);

		Object[] params = new Object[1];
		params[0] = v;

		String queryString = "SELECT d.value FROM /demo.entrySet d where d.value.blockFips =$1";
		QueryService queryService = cache.getQueryService();
		Query q = queryService.newQuery(queryString);

		SelectResults f_result = (SelectResults) q.execute(params);

		if (f_result.isEmpty()) {
			LOG.error("Query Result is Empty");
		} else

			for (Iterator iter = f_result.iterator(); iter.hasNext();) {
				PdxInstance pdx = (PdxInstance) iter.next();
				String json = JSONFormatter.toJSON(pdx);
				JSONObject obj = new JSONObject(json);
				f.put(obj);
			}
		String j = f.toString();
		return j;
	}

	public Long getFips(String lat, String lon) throws Exception {
		Long fipsCode;
		List<Long> resultF = new ArrayList<Long>();
		String queryString = "SELECT e.value.blockFips FROM /demo.entrySet e WHERE e.value.lat = " + lat
				+ " AND e.value.lon =" + lon;

		LOG.info("QueryParams: lat={}, lon={}", lat, lon);

		QueryService queryService = cache.getQueryService();
		Query query = queryService.newQuery(queryString);
		SelectResults r = (SelectResults) query.execute();

		LOG.info("Return Type : {}", r.getClass().getName());

		for (Iterator iter = r.iterator(); iter.hasNext();) {
			Long result = (Long) iter.next();
			resultF.add(result);
		}

		fipsCode = resultF.get(0);

		return fipsCode;

	}
}
