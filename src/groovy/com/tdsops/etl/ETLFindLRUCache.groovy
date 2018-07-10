package com.tdsops.etl

import com.tdssrc.grails.StringUtil
import org.codehaus.groovy.runtime.memoize.LRUProtectionStorage

/**
 * A custom LRU cache for ETL find command results..
 *
 */
class ETLFindLRUCache {

	Map<String, List<?>> cache

	ETLFindLRUCache(int maxCacheSize) {
		cache = new LRUProtectionStorage(maxCacheSize)
	}

	/**
	 *
	 * @param domainShortName
	 * @param fieldsInfo
	 * @param value
	 * @return
	 */
	List<?> put(String domainShortName, Map fieldsInfo, List<?> value) {
		String key = generateMd5OfFieldsInfoField(domainShortName, fieldsInfo)
		return cache.put(key, value)
	}

	List<?> get(String domainShortName, Map fieldsInfo) {
		String key = generateMd5OfFieldsInfoField(domainShortName, fieldsInfo)
		return cache.get(key)
	}

	private String generateMd5OfFieldsInfoField(String domainShortName, Map fieldsInfo) {
		StringUtil.md5Hex("${domainShortName}:query=${fieldsInfo}")
	}

	/**
	 * Returnes the # of objects contained in the cache
	 */
	Long size() {
		return cache.size()
	}
}