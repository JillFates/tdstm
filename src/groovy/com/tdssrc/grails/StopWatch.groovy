package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.apache.log4j.Logger

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
class StopWatch {
	static private final Logger log = Logger.getLogger(StopWatch.class);

	private tags = [:]

	/** 
	 * Constructor
	 */
	public StopWatch() {
		start('')
	}

	/**
	 * Restart the time of the chronometer
	 */
	public Date start(String tag='') {
		begin(tag)
	}

	/**
	 * Set a tag mark to Begin
	 * Every time that this function is called the time mark is reset tho the calltime
	 */
	public Date begin(String tag='') {
		Date begin = new Date()
		tags[tag] = [ begin:begin, lapTime:begin, lastLap:new TimeDuration(0,0,0,0) ]
		return begin
	}

	/**
	 * Get last Time duration of the last lap(TAG) call
	 * You should call a begin(TAG) to start the profiling block before calling this function
	 * NOTE: if this is call before invoking begin(TAG) a Warning message will be logged and a profiling block will be created.
	 */
	public TimeDuration lap(String tag='') {
		Date currentTime = new Date()
		Map clock = getClock(tag)
		clock.lastLap = elapsed(clock.lapTime, currentTime)
		clock.lapTime = currentTime
		return clock.lastLap
	}
	
	/**
	 * Get last Time duration of the last lap() call for a named Stopwatch
	 */
	public TimeDuration getLastLap(String tag='') {
		Map clock = getClock(tag)
		return clock.lastLap
	}

	/**
	 * date of the last call to lap(TAG) or 'startTime' if no begin(TAG) has been called.
	 */
	public Date getLastLapTime(String tag='') {
		Map clock = getClock(tag)
		return clock.lastLapTime
	}

	/**
	 * get the duration between the start of a named chronometer and the last call to lap()
	 */
	public TimeDuration getSinceStart(String tag='') {
		Map clock = getClock(tag)
		return elapsed(clock.begin)
	}

	/**
	 * Time when the StopWatch was created
	 */
	public Date getStartTime(String tag='') {
		Map clock = getClock(tag)
		return clock.begin
	}

	/**
	 * remove tag mark ang get duration
	 */
	public TimeDuration endDuration(String tag='') {
		Map clock = getClock(tag)
		TimeDuration elapsed = elapsed(clock.begin)
		tags.remove(tag)
		return elapsed
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
		TimeDuration e
		use (TimeCategory) {
			e = (currentTime - startTime)
		}
		return e
	}

	/**
	 * Used to retrieve a tag object which will recreate it if it was not properly created originally
	 * @param tag - the label used to reference the particular lap
	 * @return the tag object
	 */
	private Map getClock(String tag) {
		Map conf = tags[tag]
		if (conf == null) {
			log.warn("Called StopWatch with tag ($tag) before initializing it with a begin call. Initialization was graciously performed for you.")
			begin(tag)
			conf = tags[tag]
		}
		return conf
	}

	/**
	 * Used for testing to determine if a tag exists
	 * @param tag - the tag to validate exists
	 * @return true if exists else false
	 */
	protected boolean hasTag(String tag='') {
		return (tags[tag] != null)
	}
}
