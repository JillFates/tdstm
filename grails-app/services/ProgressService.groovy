import groovy.time.TimeDuration

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.tdssrc.grails.TimeUtil


/**
 * The progress services handles the logic for holding the status of async tasks
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class ProgressService {

	static final String FAILED='failed'
	static final String DONE='Completed'
	static final String PENDING='pending'
	static final String STARTED='In process'
	static final String PAUSED='paused'

	Cache<String, ProgressInfo> progressInfo 
	//REMOVE THIS. ONLY FOR DEMO
	ExecutorService service
	
	public ProgressService() {
		this.progressInfo = CacheBuilder.newBuilder()
			.expireAfterWrite(2, TimeUnit.HOURS)
			.build();
			
		//REMOVE THIS. ONLY FOR DEMO
		//this.service = Executors.newFixedThreadPool(10)
	}
	
	/**
	 * Creates a new progress info under the key and with initial status
	 * @param key the key of the progress
	 * @param status the initial status
	 */
	void create(String key, String status='Pending') {
		ProgressInfo info = new ProgressInfo(key, status)
		this.progressInfo.put(key, info)
	}
	
	// TODO : the ProgressService class will need be updated for a Clustered Tomcat configuration

	/**
	 * Updates a progress info with the specific information
	 * If the info doesn't exists it simply ignores it
	 * 
	 * @param key - the key of the progress
	 * @param percentComp - the percentage completed
	 * @param status - the initial status
	 * @param detail - any additional information that might be shown in the progress meter such as the sub-task being performed (optional)
	 * @param remainingTime - an estimate of the remainingTime before the task completes (optional)
	 */
	void update(String key, Integer percentComp, String status, String detail='', TimeDuration remainingTime=null) {
		log.debug "update() key=$key, percentComp=percentComp, status=$status"
		if (key == null) {
			log.error "update() called with null key"
			return
		} 
		ProgressInfo info = this.progressInfo.getIfPresent(key)
		if (info != null) {
			log.debug("update() Key was found ${key}")
			synchronized (info) {
				info.percentComp = percentComp
				info.status = status
				info.remainingTime = remainingTime
				info.detail = detail
				// TODO - this should be maintained in GMT
				info.lastUpdated = new Date().getTime()
			}
		} else {
			log.debug("Key not found ${key}")
		}
	}
	
	/**
	 * Adds the dataKey,dataValue to the data of the progressInfo under key
	 * @param key the key of the progressInfo
	 * @param dataKey the key of the data
	 * @param dataValue the value of the data
	 */
	void updateData(String key, Object dataKey, Object dataValue) {
		if (key == null) {
			log.error "updateData() called with null key"
			return
		} 

		ProgressInfo info = this.progressInfo.getIfPresent(key)
		if (info != null) {
			info.data.put(dataKey, dataValue)
		} else {
			log.debug("Key not found ${key}")
		}
	}
	
	/**
	 * Gets the data value of the progressInfo under key
	 * @param key the key of the progressInfo
	 * @param dataKey the key of the data
	 * @return dataValue the value of the data
	 */
	Object getData(String key, Object dataKey) {
		if (key == null) {
			log.error "getData() called with null key"
			return null
		} 

		ProgressInfo info = this.progressInfo.getIfPresent(key)
		if (info != null) {
			return info.data.get(dataKey)
		} else {
			log.debug("Key not found ${key}")
			return null
		}
	}
	
	/**
	 * Manually removes a progress info under a specific key
	 * @param key the key of the progress
	 */
	void remove(String key) {
		if (key == null) {
			log.error "remove() called with null key"
		} else {
			this.progressInfo.invalidate(key)
		}
	}
	
	/**
	 * Lists the existing progress infos in this service
	 * @return a list of maps each containing the info of the get method
	 */
	List<Map> list() {
		def results = []
		
		for (def entry : this.progressInfo.asMap().entrySet()) {
			def info = this.get(entry.getKey())
			results << [ entry.getKey(), info ]
		}
		
		return results
	}
	
	/**
	 * Returns the information about a specific key if exists and if not empty map
	 * @param key the key of the progress
	 * @return the information about a specific key if exists and if not empty map
	 */
	Map get(key) {
		if (key == null) {
			log.error "updateData() called with null key"
			return [:]
		} 

		ProgressInfo info = this.progressInfo.getIfPresent(key);
		
		if (info == null) {
			log.debug("Key not found ${key}")
			return [:]
		} else {
			log.debug("Key FOUND ${key}")
			return [
				'percentComp' : info.percentComp,
				'status' : info.status,
				'detail' : info.detail,
				'remainingTime' : info.remainingTime == null ? 'Unknown' : TimeUtil.ago(info.remainingTime),
				'lastUpdated' : info.lastUpdated
			];
		}
	}
	
	def demo() {
		def key = 'Task-' + UUID.randomUUID().toString()
		this.create(key)
		this.service.execute(new Runnable() {
			void run() {
				int p = 2
				while (p < 100) {
					ProgressService.this.update(key, p, 'In progress', null)
					p = p + 2
					Thread.sleep(1200)
				}
				ProgressService.this.update(key, 100, 'Completed', null)
			}
		});
		return ['key' : key]
	}

	def demoFailed() {
		def key = 'Task-' + UUID.randomUUID().toString()
		this.create(key)
		this.service.execute(new Runnable() {
			void run() {
				int p = 2
				while (p < 60) {
					ProgressService.this.update(key, p, 'In progress', null)
					p = p + 2
					Thread.sleep(1200)
				}
				ProgressService.this.update(key, p, 'Failed', null)
				Thread.sleep(1200)
			}
		});
		return ['key' : key]
	}
}
