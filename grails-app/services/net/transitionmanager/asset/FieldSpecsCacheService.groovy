package net.transitionmanager.asset

import groovy.transform.CompileStatic
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.transitionmanager.project.Project
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value

/**
 * <h3>Methods To manage FieldSpec saved in EhCache.</h1>
 * <p>An instance of {@code FieldSpecsCacheService} can manage FieldSpec definitions, saved in EhCache.
 * Then it can returns a deepCopy of FieldSpecs saved in cache. It returns a deep copy in order
 * to avoid changes over the instance saved in cache</p>
 * @see FieldSpecsCacheService#setAllFieldSpecs(net.transitionmanager.project.Project, java.lang.String, java.util.Map)
 * @see FieldSpecsCacheService#deepCopy(java.util.Map)
 * <p>To check a value in cache and save in cache, use the following:</p>
 * <pre>
 *   Map fieldSpec = fieldSpecsCacheService.getAllFieldSpecs(project, domain)
 *   if (!fieldSpec) {*   	...
 *   	fieldSpecsCacheService.setAllFieldSpecs(project, domain, fieldSpec)
 *}* </pre>
 * <p>To remove all field specs from cache, use the following:</p>
 * <pre>
 * 	fieldSpecsCacheService.removeFieldSpecs(project, domain)
 * </pre>
 */
@CompileStatic
class FieldSpecsCacheService implements InitializingBean {

	/**
	 * EhCache name. We can use this name to retrieve more information on /monitoring URL.
	 */
	@Value('${cache.fieldSpecs.name:FIELD_SPECS_CACHE}')
	String fieldSpecsCacheName
	@Value('${cache.fieldSpecs.maxElementsInMemory:100}')
	int maxElementsInMemory
	@Value('${cache.fieldSpecs.overflowToDisk:false}')
	boolean overflowToDisk
	@Value('${cache.fieldSpecs.external:false}')
	boolean external
	/**
	 * The maximum number of seconds an element can exist in the cache regardless of use.
	 * The element expires at this limit and will no longer be returned from the cache.
	 * The default value is 0, which means no timeToLive (TTL) eviction takes place (infinite lifetime).
	 */
	@Value('${cache.fieldSpecs.timeToLiveSeconds:0}')
	long timeToLiveSeconds
	/**
	 * The maximum number of seconds an element can exist in the cache without being accessed.
	 * The element expires at this limit and will no longer be returned from the cache.
	 * The default value is 0, which means no timeToIdle (TTI) eviction takes place (infinite lifetime).
	 */
	@Value('${cache.fieldSpecs.timeToIdleSeconds:0}')
	long timeToIdleSeconds
	/**
	 * Cache with association between project, domain and a Map of fieldSpecs
	 */
	Cache fieldSpecsCache
	/**
	 * Bean Initialization. It prepares the initial {@code FieldSpecsCacheService#fieldSpecsCache}
	 * @throws Exception
	 */
	@Override
	void afterPropertiesSet() throws Exception {
		CacheManager cacheManager = CacheManager.getInstance()
		fieldSpecsCache = cacheManager.getCache(fieldSpecsCacheName)
		if (!fieldSpecsCache) {
			log.debug("Initializing $fieldSpecsCacheName cache")
			fieldSpecsCache = new Cache(fieldSpecsCacheName, maxElementsInMemory, overflowToDisk, external, timeToLiveSeconds, timeToIdleSeconds)
			cacheManager.addCache(fieldSpecsCache)
		}
	}

	/**
	 * <p>Creates an String value with the key used to map Field Specs in
	 * {@code FieldSpecsCacheService#cache} cache </p>
	 * @param project an instance of {@code Project}
	 * @param domain a String value represents an Asset name
	 * @return a String value used as a key in {@code FieldSpecsCacheService#cache} cache
	 */
	private static String cacheKey(Project project, String domain) {
		return "project-${project.id}-${domain}"
	}

	/**
	 * <p>Creates an String value with the key used to map Field Specs in
	 * {@code FieldSpecsCacheService#cache} cache </p>
	 * @param project an instance of {@code Project}
	 * @return a String value used as a key in {@code FieldSpecsCacheService#cache} cache
	 */
	private static String cacheKey(Project project) {
		return "project-${project.id}"
	}

	/**
	 * Retrieves {@code Element} from cache
	 * @param project an instance of {@code Project}
	 * @param domain a String value represents an Asset name
	 * @return a Map of field specs or null if cache
	 * 			does not contains a value for project and domain params.
	 */
	private Map getElementValueFromCache(Project project, String domain) {
		Element element = fieldSpecsCache?.get(cacheKey(project, domain))
		return (Map) element?.value
	}
	/**
	 * <p>Retrieves a Map with all the Field Specs saves in cache for a particular Project and domain</p>
	 * <p>If it does not exists in cache it returns null</p>
	 * @param project an instance of {@code Project}
	 * @param domain a String value represents an Asset name
	 * @return a Map of field specs cloned or null if cache
	 * 			does not contains a value for project and domain params.
	 * @see FieldSpecsCacheService#deepCopy(java.util.Map)
	 */
	Map getAllFieldSpecs(Project project, String domain) {
		Map fieldSpecs = this.getElementValueFromCache(project, domain)
		return fieldSpecs ? deepCopy(fieldSpecs) : null
	}

	/**
	 * <p>Retrieves a String with all the Field Specs saves in cache for a particular Project</p>
	 * <p>If it does not exists in cache it returns null</p>
	 * @param project an instance of {@code Project}
	 * @return a String of field specs or null if cache
	 * 			does not contains a value for project param.
	 */
	String getJsonFieldSpecs(Project project) {
		Element element = fieldSpecsCache?.get(cacheKey(project))
		return element?.getObjectValue()
	}

	/**
	 * <p>Saves a String JSON content with all the Field Specs saves in cache for a particular Project</p>
	 * @param project an instance of {@code Project}
	 * @param fieldSpecs a String of field specs definitions
	 */
	void setJsonFieldSpecs(Project project, String jsonFieldSpecs) {
		fieldSpecsCache.put(new Element(cacheKey(project), jsonFieldSpecs))
	}

	/**
	 * <p>Saves a Map with all the Field Specs saves in cache for a particular Project and domain</p>
	 * @param project an instance of {@code Project}
	 * @param domain a String value represents an Asset name
	 * @param fieldSpecs a Map of field specs definitions
	 */
	void setAllFieldSpecs(Project project, String domain, Map fieldSpecs) {
		fieldSpecsCache.put(new Element(cacheKey(project, domain), deepCopy(fieldSpecs)))
	}

	/**
	 * <p>Removes field specs from cache for a particular Project and domain</p>
	 * @param project an instance of {@code Project}
	 * @param domain a String value represents an Asset name
	 */
	void removeFieldSpecs(Project project, String domain) {
		fieldSpecsCache.remove(cacheKey(project))
		fieldSpecsCache.remove(cacheKey(project, domain))
	}

	/**
	 * <p>Standard deep copy implementation</p>
	 * <p>It can create an entire copy of a Map of field specs.</p>
	 * <p>It is used by {@code FieldSpecsCacheService#getAllFieldSpecs} method</p>
	 * @param fieldSpecsMap a Map of
	 * @return a a deep copy Map from {@code fieldSpecsMap} parameter
	 */
	private Map deepCopy(Map fieldSpecsMap) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream()
		ObjectOutputStream oos = new ObjectOutputStream(bos)
		oos.writeObject(fieldSpecsMap)
		oos.flush()
		ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray())
		ObjectInputStream ois = new ObjectInputStream(bin)
		return (Map) ois.readObject()
	}
}