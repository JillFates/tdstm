import groovy.time.TimeDuration


class ProgressInfo {

	String key
	Integer percentComp
	String status
	TimeDuration remainingTime
	
	ProgressInfo(key, status) {
		this.key = key
		this.status = status
	}
}
