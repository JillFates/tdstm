package net.transitionmanager.common

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.tdssrc.grails.TimeUtil
import groovy.time.TimeDuration
import net.transitionmanager.service.ServiceMethods

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
/**
 * Handles the logic for holding the status of async tasks
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class ProgressService implements ServiceMethods {


	static transactional = false

	static final String FAILED =    'Failed'
	static final String COMPLETED = 'Completed'
	static final String PENDING =   'Pending'
	static final String STARTED =   'In progress'
	static final String PAUSED =    'Paused'

	private Cache<String, ProgressInfo> progressInfo = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build()
	private ExecutorService service = Executors.newFixedThreadPool(10)

	/**
	 * Creates a new progress info under the key and with initial status
	 * @param key the key of the progress
	 * @param status the initial status
	 */
	void create(String key, String status=PENDING) {
		ProgressInfo info = new ProgressInfo(key, status)
		info.lastUpdated = System.currentTimeMillis()
		progressInfo.put(key, info)
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
	void update(String key, Integer percentComp, String status, String detail='', TimeDuration remainingTime=null, Map data = null) {
		log.debug "update() key=$key, percentComp=$percentComp, status=$status"
		if (key == null) {
			log.error "update() called with null key"
			return
		}
		ProgressInfo info = progressInfo.getIfPresent(key)
		if (info != null) {
			// log.debug("update() Key was found $key")
			synchronized (info) {
				info.percentComp = percentComp
				info.status = status
				info.remainingTime = remainingTime
				info.detail = detail
				info.lastUpdated = System.currentTimeMillis()
				if (data) {
					info.data = data
				}

			}
		} else {
			log.debug("Key not found $key")
		}
	}

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
	void fail(String key, String detail='') {
		log.debug "fail() key=$key, detail=$detail"
		if (key == null) {
			log.error "update() called with null key"
			return
		}
		ProgressInfo info = progressInfo.getIfPresent(key)
		if (info != null) {
			log.debug("update() Key was found $key")
			synchronized (info) {
				info.percentComp = 100I
				info.status = FAILED
				//info.remainingTime = null
				info.detail = detail
				info.lastUpdated = System.currentTimeMillis()
			}
		} else {
			log.debug("Key not found $key")
		}
	}

	/**
	 * Adds the dataKey,dataValue to the data of the progressInfo under key
	 * @param key the key of the progressInfo
	 * @param dataKey the key of the data
	 * @param dataValue the value of the data
	 */
	void updateData(String key, dataKey, dataValue) {
		if (key == null) {
			log.error "updateData() called with null key"
			return
		}

		ProgressInfo info = progressInfo.getIfPresent(key)
		if (info != null) {
			info.data[dataKey] = dataValue
		} else {
			log.debug("Key not found $key")
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

		ProgressInfo info = progressInfo.getIfPresent(key)
		if (info != null) {
			info.data[dataKey]
		} else {
			log.debug("Key not found $key")
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
			progressInfo.invalidate(key)
		}
	}

	/**
	 * Lists the existing progress infos in this service
	 * @return a list of maps each containing the info of the get method
	 */
	List<Map> list() {
		def results = []

		for (entry in progressInfo.asMap().entrySet()) {
			results << [entry.key, get(entry.key)]
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

		ProgressInfo info = progressInfo.getIfPresent(key)

			if (info == null) {
			log.debug("Key not found $key")
			[:]
		} else {
			// log.debug("Key FOUND $key")
			[
				percentComp: info.percentComp,
				status: info.status,
				detail: info.detail,
				remainingTime: info.remainingTime == null ? 'Unknown' : TimeUtil.ago(info.remainingTime),
				lastUpdated: info.lastUpdated,
				data: info.data
			]
		}
	}

	def demo() {
		String key = 'Task-' + UUID.randomUUID()
		create(key)
		service.execute(new Runnable() {
			void run() {
				int p = 2
				while (p < 100) {
					ProgressService.this.update(key, p, STARTED, null)
					p = p + 2
					Thread.sleep(1200)
				}
				ProgressService.this.update(key, 100, COMPLETED, null)
			}
		})
		return [key: key]
	}

	def demoFailed() {
		def key = 'Task-' + UUID.randomUUID()
		create(key)
		service.execute(new Runnable() {
			void run() {
				int p = 2
				while (p < 60) {
					ProgressService.this.update(key, p, STARTED, null)
					p = p + 2
					Thread.sleep(1200)
				}
				ProgressService.this.update(key, p, FAILED, null)
				Thread.sleep(1200)
			}
		})
		return [key: key]
	}

	/**
	 * Given a Quartz Job key, this method will try to locate and interrupt
	 * the corresponding job, updating also the progress info cache.
	 * 
	 * Bear in mind that, in order to be interrupted, jobs must implements
	 * the InterruptableJob interface.
	 * @param jobKey
	 */
	void interruptJob(String jobKey) {

		throw new RuntimeException("Need to review implementation before this will work.")

		/* Although we still need to define what needs to be done here, I'm commenting out my first approach,
		in case that in comes in handy in the future.
		 */

		/*// Check the key corresponds to a scheduled job
		if (progressInfo.get(jobKey)) {
			 Scheduler scheduler = new StdSchedulerFactory().getScheduler()
			try {
				boolean stopped = scheduler.interrupt(jobKey)
				// If the job was successfully stopped, update the progress info for this job.
				if (stopped) {
					fail(jobKey)
				}
			} catch (UnableToInterruptJobException e) {
				// Throw an exception that ControllerMethods can handle.
				throw new RuntimeException(Message.ProgressInfoUnableToStopRunningJob)
			}
		}*/
	}
}
