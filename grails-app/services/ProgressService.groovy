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

	Cache<String, ProgressInfo> progressInfo 
	//REMOVE THIS. ONLY FOR DEMO
	ExecutorService service
	
	public ProgressService() {
		this.progressInfo = CacheBuilder.newBuilder()
			.expireAfterWrite(2, TimeUnit.HOURS)
			.build();
			
		//REMOVE THIS. ONLY FOR DEMO
		this.service = Executors.newFixedThreadPool(10)
	}
	
	/**
	 * Creates a new progress info under the key and with initial status
	 * @param key the key of the progress
	 * @param status the initial status
	 */
	void create(String key, String status='') {
		ProgressInfo info = new ProgressInfo(key, status)
		this.progressInfo.put(key, info)
		//REMOVE THIS. ONLY FOR DEMO
		this.service.execute(new Runnable() {
			void run() {
				int p = 2
				while (p < 100) {
					ProgressService.this.update(key, p, 'In progress', null)
					p = p + 2
					Thread.sleep(1000)
				}
				ProgressService.this.update(key, 100, 'Completed', null)
			}
		});
	}
	
	/**
	 * Updates a progress info with the specific information
	 * If the info doesn't exists it simply ignores it
	 * 
	 * @param key the key of the progress
	 * @param percentComp the percentage completed
	 * @param status the initial status
	 */
	void update(String key, Integer percentComp, String status, TimeDuration remainingTime=null) {
		ProgressInfo info = this.progressInfo.getIfPresent(key)
		if (info != null) {
			log.debug("Key FOUND ${key}")
			synchronized (info) {
				info.percentComp = percentComp
				info.status = status
				info.remainingTime = remainingTime
				info.lastUpdated = new Date().getTime()
			}
		} else {
			log.debug("Key not found ${key}")
		}
	}
	
	/**
	 * Manually removes a progress info under a specific key
	 * @param key the key of the progress
	 */
	void remove(String key) {
		this.progressInfo.invalidate(key)
	}
	
	/**
	 * Lists the existing progress infos in this service
	 * @return a list of maps each containing the info of the get method
	 */
	List<Map> list() {
		def results = []
		
		for (def entry : this.progressInfo.asMap().entrySet()) {
			def info = this.get(entry.getKey())
			results.add(info)
		}
		
		return results
	}
	
	/**
	 * Returns the information about a specific key if exists and if not empty map
	 * @param key the key of the progress
	 * @return the information about a specific key if exists and if not empty map
	 */
	def get(key) {
		ProgressInfo info = this.progressInfo.getIfPresent(key);
		
		if (info == null) {
			log.debug("Key not found ${key}")
			return [:]
		} else {
			log.debug("Key FOUND ${key}")
			return [
				'percentComp' : info.percentComp,
				'status' : info.status,
				'remainingTime' : info.remainingTime == null ? 'Unknown' : TimeUtil.ago(info.remainingTime),
				'lastUpdated' : info.lastUpdated
			];
		}
	}
}
