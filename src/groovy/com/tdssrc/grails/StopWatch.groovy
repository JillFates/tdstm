package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration

/**
 * Stop watch to measure the duration of execution code
 * Usage:
 *    def sw = new StopWatch()  //this starts the Clock
 *    sw.start()      // this restarts the Clock and the startTime
 *    sw.lap()    // sets the date when this function was called and returns the duration from the last call (interval from call to call)
 *
 * 	  sw.begin('tag') //sets a time mark named like the tag, return the Date when called
 * 	  sw.lap('tag')  // gets the lap from the last call of sw.lap or sw.begin related to the corresponding TAG
 * 	  sw.getLastLapTime('tag') //get the LastLapTime (Date) when we last call lap(tag)
 * 	  sw.getLastLap('tag') //get the last duration when we last call lap(tag)
 * 	  sw.endDuration('tag') //return the duration from the set of the tag 'begin(tag)' and removes the mark
 * @author @tavo_luna
 */
class StopWatch{
	private Date startTime
	private Date lastLapTime
	private tags = [:]

	private TimeDuration lap = new TimeDuration(0,0,0,0)

	public StopWatch(){
		start()
	}

	/**
	 * Restart the time of the chronometer
	 */
	public Date start(){
		startTime = new Date()
		lastLapTime = startTime
		return startTime
	}

	/**
	 * set a Lapse between the last call to lap or start, time lapse between lap() calls
	 */
	public TimeDuration lap(){
		Date currentDate = new Date()
		use (TimeCategory) {
			lap = currentDate - lastLapTime
		}
		lastLapTime = currentDate
		return lap
	}

	/**
	 * Get last Time duration of the last lap() call
	 */
	public TimeDuration getLastLap(){
		return lap
	}

	/**
	 * get Duration between the start of the chronometer and the last call to lap()
	 */
	public TimeDuration getSinceStart(){
		use (TimeCategory) {
			return (new Date()) - startTime
		}
	}

	/**
	 * Time when the StopWatch was created or the las tall to start()
	 */
	public Date getStartTime(){
		return startTime
	}

	/**
	 * date of the last call to lap() or 'startTime' in case of never been called.
	 */
	public Date getLastLapTime(){
		return lastLapTime
	}

	/**
	 * set a tag mark to Begin
	 */
	public Date begin(String tag){
		Date begin = new Date()
		tags[tag] = [ begin:begin, lapTime:begin, lastLap:new TimeDuration(0,0,0,0)]
		return begin
	}

	/**
	 * Get last Time duration of the last lap(TAG) call
	 */
	public TimeDuration lap(String tag){
		Date currentDate = new Date()

		def conf = tags[tag]
		Date lastLapTime = conf?.lapTime
		if(lastLapTime == null){
			lastLapTime = startTime
		}

		def lap = use (TimeCategory) {
			return currentDate - lastLapTime
		}

		if(conf) {
			conf.lapTime = currentDate
			conf.lastLap = lap
		}

		return lap
	}
	
	/**
	 * date of the last call to lap(TAG) or 'startTime' if no begin(TAG) has been called.
	 */
	public Date getLastLapTime(String tag) {
		Date lastLapTime = tags[tag]?.lapTime
		if (lastLapTime == null) {
			lastLapTime = startTime
		}
		return lastLapTime
	}

	/**
	 * Get last Time duration of the last lap() call
	 */
	public TimeDuration getLastLap(String tag){
		Date lastLap = tags[tag]?.lastLap
		if (lastLap == null) {
			lastLap = new TimeDuration(0,0,0,0)
		}
		return lastLap
	}

	/**
	 * remove tag mark ang get duration
	 */
	public TimeDuration endDuration(String tag){
		Date end = new Date()
		Date begin = tags[tag]?.begin
		if(begin == null){
			begin = startTime
		}else{
			tags.remove(tag)
		}
		use (TimeCategory) {
			return end - begin
		}
	}
}