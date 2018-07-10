package com.tdsops.etl

import com.tdssrc.grails.StringUtil
import org.codehaus.groovy.runtime.memoize.LRUProtectionStorage

/**
 * A custom LRU cache for ETL find command results..
 *
 */
class ETLFindCache {

	Map<String, List<?>> cache

	ETLFindCache(int maxCacheSize) {
		cache = new LRUProtectionStorage(maxCacheSize)
	}

	/**
	 * Associates the specified value with the specified key [MD5(domainClassName + fieldsInfo)]
	 * in this cache. If the cache previously contained a mapping for the key, the old
	 * value is replaced.
	 *
	 * @param domainClassName class name from a domain class in system
	 * @param fieldsInfo a map with key and value used in a find command
	 * @param value a List of objects to be associated with the specified key
	 * @return the previous value associated with <tt>key</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 *         (A <tt>null</tt> return can also indicate that the map
	 *         previously associated <tt>null</tt> with <tt>key</tt>.)
	 */
	List<?> put(String domainClassName, Map fieldsInfo, List<?> value) {
		String key = generateMd5Key(domainClassName, fieldsInfo)
		return cache.put(key, value)
	}

	/**
	 * Returns the value to which the specified key [MD5(domainClassName + fieldsInfo)] is mapped,
	 * or {@code null} if this cahe contains no mapping for the key. <br>
	 * It uses {@code generateMd5KeyFrom} method to build the key
	 * based on {@link StringUtil#md5Hex(java.lang.String)}
	 *
	 * @param domainClassName class name from a domain class in system
	 * @param fieldsInfo a map with key and value used in a find command
	 * @return <p>if this map contains a mapping from a key {@code k} to a value {@code v}
	 *          such that {@code ( key = = null ? k = = null : key.equals ( k ) )},
	 *          then this method returns {@code v}; otherwise
	 *          it returns {@code null}.  (There can be at most one such mapping.)
	 */
	List<?> get(String domainClassName, Map fieldsInfo) {
		String key = generateMd5Key(domainClassName, fieldsInfo)
		return cache.get(key)
	}

	/**
	 * Creates a key for this cache converting an string
	 * based on domainClassName and fieldsInfo parameters
	 * in a MD5 string content.
	 * <pre>
	 *  generateMd5Key('Application', [assetName: 'VM Cluster: AMSCPESX101']) == 'b41d3f096fc492b8f022b9f4640187f5'
	 * </pre>
	 *
	 * @param domainClassName class name from a domain class in system
	 * @param fieldsInfo a map with key and value used in a find command
	 * @return an MD5 string based on parameters
	 * @see StringUtil#md5Hex(java.lang.String)
	 */
	private String generateMd5Key(String domainClassName, Map fieldsInfo) {
		return StringUtil.md5Hex("${domainClassName}:query=${fieldsInfo}")
	}

	/**
	 * Return the # of objects contained in the cache
	 */
	Long size() {
		return cache.size()
	}
}