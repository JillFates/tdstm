import groovy.time.TimeDuration
import java.util.concurrent.ConcurrentHashMap


/**
 * The progress services handles the logic for holding the status of async tasks
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class ProgressService {

	Map<String, ProgressInfo> progressInfo
	
	public ProgressService() {
		this.progressInfo = new ConcurrentHashMap<String, ProgressInfo>();
	}
	
	void create(String key, String status='', Integer ttl=50) {
		ProgressInfo info = new ProgressInfo(key, status)
		this.progressInfo.put(key, info)
	}
	
	void update(String key, Integer percentComp, String status, TimeDuration remainingTime=null) {
		ProgressInfo info = this.progressInfo.get(key)
		if (info != null) {
			synchronized (info) {
				info.percentComp = percentComp
				info.status = status
				info.remainingTime = remainingTime
			}
		}
	}
	
	void remove(String key) {
		this.progressInfo.remove(key)
	}
	
	List<Map> list() {
		def results = []
		
		for (def entry : this.progressInfo.entrySet()) {
			results.add(this.get(entry.getKey()))
		}
		
		return results
	}
	
	def get(id) {
		//TODO esteban
		return [
			'percentComp' : 20,
			'status' : 'In progress',
			'remainingTime' : '2 min 12 sec',
			'lastUpdated' : new Date().getTime()
		];
	}
}
