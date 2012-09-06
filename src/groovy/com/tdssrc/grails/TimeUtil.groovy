package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration


/**
 * The TimeUtil class contains a collection of useful Time manipulation methods 
 */
class TimeUtil {
	
	/**
	 * Returns the elapsed duration that occured between two date objects as a groovy.time.TimeDuration
	 * @param Date	starting Datetime
	 * @param Date	ending datetime
	 * @return TimeDuration
	 */
	public static TimeDuration elapsed(def start, def end) {
		if (! start || ! end ) {
			return new TimeDuration(0,0,0,0)
		}
		use (TimeCategory) {
			TimeDuration duration = end - start
			return duration
		}
	}

	/**
	 * Returns a string that represents the time in shorthand that follows these rules:
	 * > 24 hours - ##d ##h ##s
	 * > 1 hour - ##h ##m ##s
	 * <= 90 minutes - ##m ##s
	 * >1 minute - ##s
	 * @param TimeDuration	The elapsed duration
	 */ 
	public static String ago(TimeDuration duration) {		
		int days=duration.getDays()
		int hours=duration.getHours()
		int minutes=duration.getMinutes()
		int seconds=duration.getSeconds()
		String ago = ""
        if (days > 0) {
                ago = "${days}d ${hours}h ${minutes}m"
        } else if (hours > 0) {
                ago = "${hours}h ${minutes}m ${seconds}s"
        } else if (minutes > 0) {
                ago = "${minutes}m ${seconds}s"
        } else {
                ago = "${seconds}s"
        }
		return ago
	}

	/**
	 * Overloaded variation of the ago(TimeDuration) method that accepts a start and ending time
	 * @param Date 	a starting datetime
	 * @param Date	an ending datetime
	 * @return String	The elapsed time in shorthand
	 */
	public static String ago(def start, def end) {
		return ago( elapsed(start, end) )
	}

}