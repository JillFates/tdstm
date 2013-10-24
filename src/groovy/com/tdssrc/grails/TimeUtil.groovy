package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import java.text.SimpleDateFormat

/**
 * The TimeUtil class contains a collection of useful Time manipulation methods 
 */
class TimeUtil {
	def static timeZones = [GMT:"GMT-00:00", PST:"GMT-08:00", PDT:"GMT-07:00", MST:"GMT-07:00", MDT:"GMT-06:00", 
							CST:"GMT-06:00", CDT:"GMT-05:00", EST:"GMT-05:00",EDT:"GMT-04:00"]
	
	/**
	 * Used to adjust a datetime by adding or subtracting a specified number of DAYS from an existing date
	 * @param Date	a date to be adjusted
	 * @param Integer	the amount to adjust either positive or negative
	 * @return Date	the adjusted date
	 */
	public static Date adjustDays(date, adjustment) {
		TimeDuration td = new TimeDuration(adjustment,0,0,0,0)
		return TimeCategory.plus(date, td)
	}
	/**
	 * Used to adjust a datetime by adding or subtracting a specified number of HOURS from an existing date
	 * @param Date	a date to be adjusted
	 * @param Integer	the amount to adjust either positive or negative
	 * @return Date	the adjusted date
	 */
	public static Date adjustHours(date, adjustment) {
		TimeDuration td = new TimeDuration(adjustment,0,0,0)
		return TimeCategory.plus(date, td)
	}
	/**
	 * Used to adjust a datetime by adding or subtracting a specified number of MINUTES from an existing date
	 * @param Date	a date to be adjusted
	 * @param Integer	the amount to adjust either positive or negative
	 * @return Date	the adjusted date
	 */
	public static Date adjustMinutes(date, adjustment) {
		TimeDuration td = new TimeDuration(0,adjustment,0,0)
		return TimeCategory.plus(date, td)
	}
	/**
	 * Used to adjust a datetime by adding or subtracting a specified number of SECONDS from an existing date
	 * @param Date	a date to be adjusted
	 * @param Integer	the amount to adjust either positive or negative
	 * @return Date	the adjusted date
	 */
	public static Date adjustSeconds(date, adjustment) {
		TimeDuration td = new TimeDuration(0,0,adjustment,0)
		return TimeCategory.plus(date, td)
	}
	
	/**
	 * Returns the elapsed duration that occured between a start time and now
	 * @param Date	starting Datetime
	 * @return TimeDuration
	 */
	public static TimeDuration elapsed(def start) {
		return elapsed(start, new Date())
	}

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

	/**
	 * Get the current datetime in GMT
	 * @return Date The current date set in GMT
	 */
	def public static nowGMT() {
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss")
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"))
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss")
		
		return dateFormatLocal.parse( dateFormatGmt.format(new Date()) )
	}
	
	/**
	 * Get the current datetime in GMT
	 * @return Date The current date set in GMT in sql format date
	 */
	def public static nowGMTSQLFormat() {
		SimpleDateFormat sqlFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		sqlFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"))
		return sqlFormatGmt.format(new Date())
	}
	
	/**
	 * Converts date into GMT
	 * @param date
	 * @return converted Date
	 */
	def public static convertInToGMT = { date, tzId ->
		Date ret
		if(date){
			TimeZone tz
			if(date == 'now'){
				tz  = TimeZone.getDefault()
				Calendar calendar = Calendar.getInstance(tz);
				date = calendar.getTime()
				ret = new Date(date.getTime() - tz.getRawOffset());
				// if we are now in DST, back off by the delta. Note that we are
				// checking the GMT date, this is the KEY.
				if (tz.inDaylightTime(ret)) {
					Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());
					// check to make sure we have not crossed back into standard time
					// this happens when we are on the cusp of DST (7pm the day before
					// the change for PDT)
					if (tz.inDaylightTime(dstDate))	{
						ret = dstDate;
					}
				}
			} else {
				tzId = tzId ? tzId : "EDT"
				def timeZoneId = timeZones[ tzId ]
				tz = TimeZone.getTimeZone( timeZoneId )
				ret = new Date(date.getTime() - tz.getRawOffset());
			}
		}
		return ret;
		
	}
	/**
	 * Converts date from GMT to local format
	 * @param date
	 * @return converted Date
	 */
	def public static convertInToUserTZ = { date, tzId ->
		Date ret
		if (date) {
			tzId = tzId ? tzId : "EDT"
			def timeZoneId = timeZones[ tzId ]
			TimeZone tz = TimeZone.getTimeZone( timeZoneId );
			//java.sql.Timestamp
			try {
				ret = new Date(date.getTime() + tz.getRawOffset());				
			} catch (e) { 
				// log.error "convertInToUserTZ(${date}, ${tzId}) had exception: e.toString()" 
			}
			
			// println "convertInToUserTZ() date=${date}, tzId=${tzId}, newDate=${ret}"
			// if we are now in DST, back off by the delta. Note that we are
			// checking the GMT date, this is the KEY.
			/*if (tz.inDaylightTime(ret)) {
				Date dstDate = new Date(ret.getTime() + tz.getDSTSavings());
				// check to make sure we have not crossed back into standard time
				// this happens when we are on the cusp of DST (7pm the day before
				// the change for PDT)
				if (tz.inDaylightTime(dstDate))	{
					ret = dstDate;
				}
			}*/
		}
		return ret;
	}

	/**
	 * Used to convert a string into a date that includes the Timezone
	 * @param the datetime as a string
	 * @return The date or null if it failed to parse it
	 **/
	def public static Date parseDateTime( String text) {
		def format = new SimpleDateFormat("MM/dd/yyyy hh:mma z")

		def dt

		try {
			dt = format.parse(text)
		} catch (java.text.ParseException e) {
			// println "parseDateTime() invalid formated string $text"
		}
		return dt
	}

	/**
	 * Used to get the current time in GMT
	 * @return Date 	The current datetime in GMT
	 */
	/*public static Date nowGMT() {
		return convertInToGMT("now", "EDT" )
	}*/
}