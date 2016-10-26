package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

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
@CompileStatic
@Slf4j(value='logger')
class StopWatch {

	@CompileStatic
	static class Clock {
		Date begin = new Date()
		Date lapTime = begin
		TimeDuration lastLap = new TimeDuration(0, 0, 0, 0)
	}

	private Map<String, Clock> tags = [:]

	StopWatch() {
		start()
	}

	/**
	 * Restart the time of the chronometer
	 */
	Date start(String tag='') {
		begin(tag)
	}

	/**
	 * Set a tag mark to Begin
	 * Every time that this function is called the time mark is reset tho the calltime
	 */
	Date begin(String tag='') {
		tags[tag] = new Clock()
		tags[tag].begin
	}

	/**
	 * Get last Time duration of the last lap(TAG) call
	 * You should call a begin(TAG) to start the profiling block before calling this function
	 * NOTE: if this is call before invoking begin(TAG) a Warning message will be logged and a profiling block will be created.
	 */
	TimeDuration lap(String tag='') {
		Date currentTime = new Date()
		Clock clock = getClock(tag)
		clock.lastLap = elapsed(clock.lapTime, currentTime)
		clock.lapTime = currentTime
		return clock.lastLap
	}

	/**
	 * Get last Time duration of the last lap() call for a named Stopwatch
	 */
	TimeDuration getLastLap(String tag='') {
		getClock(tag).lastLap
	}

	/**
	 * date of the last call to lap(TAG) or 'startTime' if no begin(TAG) has been called.
	 */
	Date getLastLapTime(String tag='') {
		null // getClock(tag).lastLapTime
	}

	/**
	 * get the duration between the start of a named chronometer and the last call to lap()
	 */
	TimeDuration getSinceStart(String tag='') {
		elapsed(getClock(tag).begin)
	}

	/**
	 * Time when the StopWatch was created
	 */
	Date getStartTime(String tag='') {
		getClock(tag).begin
	}

	/**
	 * remove tag mark ang get duration
	 */
	TimeDuration endDuration(String tag='') {
		TimeDuration elapsed = elapsed(getClock(tag).begin)
		tags.remove(tag)
		elapsed
	}

	// ----------------------------------
	// Private methods
	// ----------------------------------

	/**
	 * get Duration between the start of the chronometer and the currentTime parameter
	 * @param startTime - the time of the chronometer started
	 * @param currentTime - the time that the chronometer was stopped, defaults to now
	 * @return the time elapsed between the two times
	 */
	private TimeDuration elapsed(Date startTime, Date currentTime=new Date()) {
		TimeCategory.minus currentTime, startTime
	}

	/**
	 * Used to retrieve a tag object which will recreate it if it was not properly created originally
	 * @param tag - the label used to reference the particular lap
	 * @return the tag object
	 */
	private Clock getClock(String tag) {
		Clock clock = tags[tag]
		if (!clock) {
			logger.warn('Called StopWatch with tag ({}) before initializing it with a start call. Initialization was graciously performed for you.', tag)
			start(tag)
			clock = tags[tag]
		}
		return clock
	}

	/**
	 * Determine if a tag exists.
	 * @param tag - the tag to validate exists
	 * @return true if exists
	 */
	protected boolean hasTag(String tag='') {
		tags[tag]
	}
}
