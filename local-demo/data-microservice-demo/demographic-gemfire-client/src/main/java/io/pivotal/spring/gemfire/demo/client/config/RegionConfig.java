package io.pivotal.spring.gemfire.demo.client.config;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.pdx.PdxInstance;

@Component
public class RegionConfig {
	private static final Logger LOG = LoggerFactory.getLogger(RegionConfig.class);
	
	@Resource(name = "demo-region")
	public Region<String, PdxInstance> region;

	@Bean(name = "demo-region")
	public Region<String, PdxInstance> createDemoRegion(ClientCache cache, Pool pool) {
		LOG.info("creating demo-region");
		ClientRegionFactory<String, PdxInstance> crf = cache.createClientRegionFactory(ClientRegionShortcut.PROXY);
		crf.setPoolName(pool.getName());
		Region<String, PdxInstance> r = crf.create("demo");
		return r;
	}

}