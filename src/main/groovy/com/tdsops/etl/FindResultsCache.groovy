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

	CachingLinkedHashMap<String, List<?>> cache
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

	/**
	 * Extending the LinkedHashMap is necessary to allow setting the MAX ENTRIES
	 * TODO : JPM 7/2018 : split out and have constructor that accepts max and initial size
	*/
	public class CachingLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

		private int maxCacheEntries = 0

		public CachingLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
			super(initialCapacity, loadFactor, accessOrder);
			maxCacheEntries = initialCapacity
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > maxCacheEntries
		}

		int getMaxCacheEntries() {
			return maxCacheEntries
		}
	}

	/**
	 * Constructor
	 */
	FindResultsCache(Integer initialSize = MAX_ENTRIES) {
		// Retain the cache max size
		cache = new CachingLinkedHashMap<String, List<?>>(initialSize + 1, 0.75F, true)
	}

	/**
	 * Associates the specified value with the specified key [MD5(domainClassName + fieldsInfo)]
	 * in this findCache. If the findCache previously contained a mapping for the key, the old
	 * value is replaced.
	 *
	 * @param domainClassName class name from a domain class in system
	 * @param conditions a list of {@code FindCondition} used in a find command
	 * @param value a List of objects to be associated with the specified key
	 * @return the previous value associated with <tt>key</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 *         (A <tt>null</tt> return can also indicate that the map
	 *         previously associated <tt>null</tt> with <tt>key</tt>.)
	 */
	List<?> put(String domainClassName, List<FindCondition> conditions, List<?> value) {
		String key = generateMd5Key(domainClassName, conditions)
		return cache.put(key, value)
	}

	/**
	 * Returns the value to which the specified key [MD5(domainClassName + fieldsInfo)] is mapped,
	 * or {@code null} if this cahe contains no mapping for the key. <br>
	 * It uses {@code generateMd5KeyFrom} method to build the key
	 * based on {@link StringUtil#md5Hex(java.lang.String)}
	 *
	 * @param domainClassName class name from a domain class in system
	 * @param conditions a list of {@code FindCondition} used in a find command
	 * @return <p>if this map contains a mapping from a key {@code k} to a value {@code v}
	 *          such that {@code ( key = = null ? k = = null : key.equals ( k ) )},
	 *          then this method returns {@code v}; otherwise
	 *          it returns {@code null}.  (There can be at most one such mapping.)
	 */
	List<?> get(String domainClassName, List<FindCondition> conditions) {
		accessCount++
		String key = generateMd5Key(domainClassName, conditions)
		List<?> value = cache.get(key)
		updateHitCount(value != null)
		return value
	}

	/**
	 * Updates the hit count for reporting the hit ratio
	 */
	private void updateHitCount(boolean found) {
		if (found) {
			hitCount++
		}
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
	private String generateMd5Key(String domainClassName, List<FindCondition> conditions) {
		return StringUtil.md5Hex("${domainClassName}:query=${conditions}")
	}

	/**
	 * Return the # of objects contained in the findCache
	 */
	Long size() {
		return cache ? cache.size() : 0
	}

	Long getAccessCount() {
		return accessCount
	}

	Long getHitCount() {
		return hitCount
	}

	/**
	 * Used to access the max size of the cache
	 */
	Long cacheMaxSize() {
		return cache.getMaxCacheEntries()
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
