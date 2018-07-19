package com.tdsops.etl

import com.tdssrc.grails.StringUtil

/**
 * A custom LRU findCache for ETL find command results.<br>
 * Following some considerations from java documentations, this findCache uses
 * an instance of LinkedHashMap and {@code LinkedHashMap # removeEldestEntry} method
 * that includes a way to remove the least-recently accessed entries automatically.
 *
 * @link https://docs.oracle.com/javase/tutorial/collections/implementations/map.html
 */
class FindResultsCache {

	Map<String, List<?>> cache
	/**
	 * Initial size of LRU findCache
	 */
	static final Integer MAX_ENTRIES = 10000
	/**
	 * Counter for the number of accesses
	 */
	private long accessCount = 0
	/**
	 * Counter for the number of hits
	 */
	private long hitCount = 0

	FindResultsCache(Integer initialSize = MAX_ENTRIES) {

		cache = new LinkedHashMap<String, List<?>>(initialSize + 1, 0.75F, true) {
			/**
			 * Overrides a default implementation in LinkedHashMap and is where
			 * we determine the policy for removing the oldest entry.
			 * In this case, we return true when the findCache has
			 * more entries than our defined capacity.
			 * @param eldest The least recently inserted entry in the map, or if
			 *           this is an access-ordered map, the least recently accessed
			 *           entry.  This is the entry that will be removed it this
			 *           method returns <tt>true</tt>.  If the map was empty prior
			 *           to the <tt>put</tt> or <tt>putAll</tt> invocation resulting
			 *           in this invocation, this will be the entry that was just
			 *           inserted; in other words, if the map contains a single
			 *           entry, the eldest entry is also the newest.
			 *
			 * @return <tt>true</tt> if the eldest entry should be removed
			 *          from the map; <tt>false</tt> if it should be retained.
			 */
			@Override
			protected boolean removeEldestEntry(Map.Entry eldest) {
				return size() > MAX_ENTRIES
			}
		}
	}

	/**
	 * Associates the specified value with the specified key [MD5(domainClassName + fieldsInfo)]
	 * in this findCache. If the findCache previously contained a mapping for the key, the old
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
		accessCount++
		String key = generateMd5Key(domainClassName, fieldsInfo)
		List<?> value = cache.get(key)
		hitCount = (value == null) ? hitCount : hitCount + 1
		return value
	}

	/**
	 * Creates a key for this findCache converting an string
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
	 * Return the # of objects contained in the findCache
	 */
	Long size() {
		return cache.size()
	}

	Long getAccessCount() {
		return accessCount
	}

	Long getHitCount() {
		return hitCount
	}

	/**
	 * Returns how many findCache accesses are "hits" - that is,
	 * how many times the required data was found in the findCache
	 * for a given number of accesses.
	 * Hit rate is usually expressed as a percentage
	 * @return percentage of hit counts over access count
	 */
	Double hitCountRate(){
		if(accessCount == 0){
			return 0.0
		} else {
			double percent = (hitCount / accessCount ) * 100
			return percent.round(2)
		}
	}
}
