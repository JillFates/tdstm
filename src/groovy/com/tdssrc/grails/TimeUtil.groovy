package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * The TimeUtil class contains a collection of useful Time manipulation methods 
 */
class TimeUtil {
	//TODO: Remove!!!
	def static timeZones = [GMT:"GMT-00:00", PST:"GMT-08:00", PDT:"GMT-07:00", MST:"GMT-07:00", MDT:"GMT-06:00", 
							CST:"GMT-06:00", CDT:"GMT-05:00", EST:"GMT-05:00",EDT:"GMT-04:00"]
	def static final dateTimeFormats = ["MM/DD/YYYY", "DD/MM/YYYY"]
	def static final defaultTimeZone = "GMT"
	def static final GMT = "GMT"

	def static final String TIMEZONE_ATTR = "CURR_TZ"
	def static final String DATE_TIME_FORMAT_ATTR = "CURR_DT_FORMAT"

	def static dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mma z")
	def static dateFormat = new SimpleDateFormat("MM/dd/yyyy")

	// Valid date time formats
	def static final FORMAT_DATE = "MM/dd/yyyy"
	def static final FORMAT_DATE_TIME = "MM/dd/yyyy hh:mm a"
	def static final FORMAT_DATE_TIME_2 = "MM-dd-yyyy hh:mm:ss a"
	def static final FORMAT_DATE_TIME_3 = "E, d MMM 'at ' HH:mma"
	def static final FORMAT_DATE_TIME_4 = "MM/dd kk:mm"
	def static final FORMAT_DATE_TIME_5 = "yyyyMMdd"
	def static final FORMAT_DATE_TIME_6 = "yyyy-MM-dd"
	def static final FORMAT_DATE_TIME_7 = "dd-MMM"
	def static final FORMAT_DATE_TIME_8 = "MMM dd,yyyy hh:mm a"
	def static final FORMAT_DATE_TIME_9 = "MM-dd-yyyy hh:mm a"
	def static final FORMAT_DATE_TIME_10 = "MMM dd"
	def static final FORMAT_DATE_TIME_11 = "yyyy/MM/dd hh:mm:ss a"
	def static final FORMAT_DATE_TIME_12 = "MM-dd-yyyy"
	def static final FORMAT_DATE_TIME_13 = "MM/dd kk:mm:ss"
	// Used in sql queries
	def static final FORMAT_DATE_TIME_14 = "yyyy-MM-dd hh:mm"
	def static final FORMAT_DATE_TIME_15 = "yyyy-MM-dd HH:mm:ss"
	def static final FORMAT_DATE_TIME_16 = "yyyy-MM-dd hh:mm a"

	static final String SHORT='S'
	static final String FULL='F'
	static final String ABBREVIATED='A'

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
	 * @param format - the format of the time units (SHORT- h/m/s, ABBREVIATED hr/min/sec, FULL hour/minute/second) default SHORT
	 * @return The time formatted
	 * @examples
	 *     3d 4h 10m
	 *     3-days 4-hrs 10-min
	 *     10-min 5-sec
	 *     3-days 4-hours 10-minutes 
	 */ 
	public static String ago(TimeDuration duration, String format=SHORT) {	
		StringBuffer ago = new StringBuffer()
		String space = ''

		def days = duration.getDays()
		def hours = duration.getHours()
		def minutes = duration.getMinutes()
		def seconds = duration.getSeconds()

		// local closure to do the borrowing math on the d:h:m:s appropriately
		def adjustForNegative = { ggrand, grand, parent, value, factor -> 
			if (value < 0) {
				if (ggrand == 0 && grand == 0 && parent == 0 ) {
					// We stop borrowing and just flip the value from negative to positive
					value *= -1
				} else { 
					// Borrow from the parent
					int adj = value.intdiv(factor) + 1
					value = factor + value
					if (parent < 0)
						parent += adj
					else
						parent -= adj
				}
			} else {
				// See if we should borrow from the parent to knock it down
				if (parent < 0 && value > 0) {
					parent++
					value = factor - value
				}
			}
			return [value, parent]
		}

		if (days < 0 || hours < 0 || minutes < 0 || seconds < 0) {
			(seconds, minutes) = adjustForNegative(days, hours, minutes, seconds, 60)
			(minutes, hours) = adjustForNegative(0, days, hours, minutes, 60)
			(hours, days) = adjustForNegative(0, 0, days, hours, 60)

			ago.append('-')
		}

        if (days != 0) {
        	ago.append("${days}${ ( format == SHORT ? 'd' : ( '-day' + (days == 1 ? '' : 's') ) ) }")
        } 
        // Hours
        if (hours != 0) {
			space = (ago.length() > 1 ? ' ' : '')
			ago.append("${space}${hours}${ ( format == SHORT ? 'h' : ( format==FULL ? '-hour' : '-hr' ) + ( hours == 1 ? '' : 's') ) }") 
        }
        // Minutes
        if ( days == 0 && minutes > 0 ) {
			space = (ago.length() > 1 ? ' ' : '')
			ago.append("${space}${minutes}${ ( format == SHORT ? 'm' : ( format==FULL ? '-minute' : '-min' ) + (minutes == 1 ? '' : 's') ) }") 
        } 
        // Only show seconds if day/hr are zero
        if ( days == 0 && hours == 0 && seconds > 0 ) {
			space = (ago.length() > 1 ? ' ' : '')
			ago.append("${space}${seconds}${ ( format == SHORT ? 's' : ( format==FULL ? '-second' : '-sec' ) + (seconds == 1 ? '' : 's') ) }") 
        }

		return ago.toString()
	}

	/**
	 * Overload variation of the ago method that accepts a integer in seconds
	 * @param secs - the number of seconds
	 * @return The time in shorthand
	 **/
	public static String ago(Integer secs, String format=SHORT) {
		Date start = new Date()
		Date end
		use( TimeCategory ) {
		    end = start + (secs).seconds
		}
		return ago( elapsed(start, end), format)
	}

	/**
	 * Overloaded variation of the ago(TimeDuration) method that accepts a start and ending time
	 * @param Date 	a starting datetime
	 * @param Date	an ending datetime
	 * @return String	The elapsed time in shorthand
	 */
	public static String ago(Date start, Date end, String format=SHORT) {
		return ago( elapsed(start, end), format )
	}

	/**
	 * Get the current datetime in GMT
	 * @return Date The current date set in GMT
	 */
	def public static nowGMT() {
		return new Date()
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

	public static String getDateTimeFormat(value) {
		def result = dateTimeFormats[0]
		dateTimeFormats.each{ df ->
			if (df == value) {
				result = df
			}
		}
		return result
	}

	public static String formatDate(session, dateValue) {
		def formatter = createFormatter(session, FORMAT_DATE)
		return formatDateTime(session, dateValue, formatter)
	}

	public static String formatDateTime(session, dateValue, String formatterType=FORMAT_DATE_TIME) {
		def formatter = createFormatter(session, formatterType)
		return formatDateTime(session, dateValue, formatter)
	}

	public static String formatDateTime(session, dateValue, DateFormat formatter) {
		def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
		return formatDateTimeWithTZ(tzId, dateValue, formatter)
	}

	public static String formatDateTimeWithTZ(tzId, dateValue, String formatterType=FORMAT_DATE_TIME) {
		def formatter = createFormatter(null, FORMAT_DATE)
		return formatDateTimeWithTZ(tzId, dateValue, formatter)
	}

	public static String formatDateTimeWithTZ(tzId, dateValue, DateFormat formatter) {
		formatter.setTimeZone(TimeZone.getTimeZone(tzId))
		return formatter.format(dateValue)
	}

	public static String formatDateTimeAsGMT(dateValue, String formatterType=FORMAT_DATE_TIME) {
		return formatDateTimeWithTZ(GMT, dateValue, formatterType)
	}

	/**
	 * Used to convert a string into a date that includes the Timezone
	 * @param the datetime as a string
	 * @return The date or null if it failed to parse it
	 **/
	public static Date parseDate(session, dateValue) {
		def formatter = createFormatter(FORMAT_DATE)
		def newDate = parseDateTime(session, dateValue, formatter)
		newDate.clearTime()
		return newDate
	}

	/**
	 * Used to convert a string into a date that includes the Timezone
	 * @param the datetime as a string
	 * @return The date or null if it failed to parse it
	 **/
	public static Date parseDateTime(session, dateValue, String formatterType=FORMAT_DATE_TIME) {
		def formatter = createFormatter(formatterType)
		return parseDateTime(session, dateValue, formatter)
	}

	public static Date parseDateTime(session, dateValue, DateFormat formatter) {
		def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
		formatter.setTimeZone(TimeZone.getTimeZone(tzId))
		return formatter.parse(dateValue)
	}

	/**
	 * Parse a time value in users timezone and creates a date into GMT
	 * @param session
	 * @param timeValue
	 * @return converted Date
	 */
	public static Date parseTime(session, timeValue) {
		Date ret
		if(timeValue){
			def date = new Date(timeValue)
			def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
			TimeZone tz = TimeZone.getTimeZone( tzId )
			ret = new Date(date.getTime() - tz.getRawOffset());
		}
		return ret;
	}

	private static DateFormat createFormatter(session, String formatterType) {
		def formatter
		switch (formatterType) {
			case FORMAT_DATE:
				formatter = new SimpleDateFormat("MM/dd/yyyy")
				break;
			case FORMAT_DATE_TIME:
				formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
				break;
			case FORMAT_DATE_TIME_2:
				formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a")
				break;
			case FORMAT_DATE_TIME_3:
				formatter = new SimpleDateFormat("E, d MMM 'at ' HH:mma")
				break;
			case FORMAT_DATE_TIME_4:
				formatter = new SimpleDateFormat("MM/dd kk:mm")
				break;
			case FORMAT_DATE_TIME_5:
				formatter = new SimpleDateFormat("yyyyMMdd")
				break;
			case FORMAT_DATE_TIME_6:
				formatter = new SimpleDateFormat("yyyy-MM-dd")
				break;
			case FORMAT_DATE_TIME_7:
				formatter = new SimpleDateFormat("dd-MMM")
				break;
			case FORMAT_DATE_TIME_8:
				formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a")
				break;
			case FORMAT_DATE_TIME_9:
				formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a")
				break;
			case FORMAT_DATE_TIME_10:
				formatter = new SimpleDateFormat("MMM dd")
				break;
			case FORMAT_DATE_TIME_11:
				formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a")
				break;
			case FORMAT_DATE_TIME_12:
				formatter = new SimpleDateFormat("MM-dd-yyyy")
				break;
			case FORMAT_DATE_TIME_13:
				formatter = new SimpleDateFormat("MM/dd kk:mm:ss")
				break;
			case FORMAT_DATE_TIME_14:
				formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm")
				break;
			case FORMAT_DATE_TIME_15:
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				break;
			case FORMAT_DATE_TIME_16:
				formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a")
				break;
		}
		return formatter
	}

}
