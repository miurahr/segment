package net.loomchild.segment.srx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents SRX document cache.
 * Responsible for managing cached data.
 * 
 * @author loomchild
 */
public class SrxDocumentCache {
	
	private final Map<String, Object> map;
	
	public SrxDocumentCache() {
		this.map = new ConcurrentHashMap<>();
	}
	
	/**
	 * Retrieves object from cache. 
	 * @param key
	 * @return value object
	 */
	public Object get(String key) {
        return map.get(key);
	}

	/**
	 * Puts an object in cache.
	 * @param key
	 * @param value value object
	 */
	public void put(String key, Object value) {
		map.put(key, value);
	}
	
}
