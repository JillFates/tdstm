import groovy.time.TimeDuration


class ProgressInfo {

	String key
	Integer percentComp = 0
	String status
	String detail
	TimeDuration remainingTime
	long lastUpdated
	
	ProgressInfo(key, status) {
		this.key = key
		this.status = status
	}
}
