package net.transitionmanager.common

import groovy.time.TimeDuration
import groovy.transform.CompileStatic

@CompileStatic
class ProgressInfo {

	String key
	Integer percentComp = 0
	String status
	String detail
	TimeDuration remainingTime
	long lastUpdated
	Map data = [:]

	ProgressInfo(String key, String status) {
		this.key = key
		this.status = status
	}
}
