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
	def static final dateTimeFormatTypes = ["MM/DD/YYYY", "DD/MM/YYYY"]
	def static final defaultTimeZone = "GMT"

	def static final String TIMEZONE_ATTR = "CURR_TZ"
	def static final String DATE_TIME_FORMAT_ATTR = "CURR_DT_FORMAT"

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
	def static final FORMAT_DATE_TIME_14 = "yyyy-MM-dd hh:mm" //Used in queries
	def static final FORMAT_DATE_TIME_15 = "yyyy-MM-dd HH:mm:ss" //Used in queries
	def static final FORMAT_DATE_TIME_16 = "yyyy-MM-dd hh:mm a" //Used in queries
	def static final FORMAT_DATE_TIME_17 = "MM/dd"
	def static final FORMAT_DATE_TIME_18 = "M/d"
	def static final FORMAT_DATE_TIME_19 = "M/d kk:mm"
	def static final FORMAT_DATE_TIME_20 = "hh:mm"
	def static final FORMAT_DATE_TIME_21 = "mm/dd"

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

	/**
	 * Check if value is a valid format type and if not returns default
	 * @return The format type
	 */
	public static String getDateTimeFormatType(value) {
		def result = getDefaultFormatType()
		dateTimeFormatTypes.each{ df ->
			if (df == value) {
				result = df
			}
		}
		return result
	}

	/**
	 * Default format type
	 * @return The default format type
	 **/
	public static getDefaultFormatType() {
		return dateTimeFormatTypes[0];
	}

	/**
	 * Used to format a Date into a date string format, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @return The date formatted
	 **/
	public static String formatDate(session, dateValue) {
		def formatter = createFormatter(session, FORMAT_DATE)
		return formatDateTime(session, dateValue, formatter)
	}

	/**
	 * Used to format a Date into a string format, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @param the formatterType defines the format to be used
	 * @return The date formatted
	 **/
	public static String formatDateTime(session, dateValue, String formatterType=FORMAT_DATE_TIME) {
		def formatter = createFormatter(session, formatterType)
		return formatDateTime(session, dateValue, formatter)
	}

	/**
	 * Used to format a Date into a string format, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @param the formatter defines the formatter to be used
	 * @return The date formatted
	 **/
	public static String formatDateTime(session, dateValue, DateFormat formatter) {
		def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
		return formatDateTimeWithTZ(tzId, dateValue, formatter)
	}

	/**
	 * Used to format a Date into a string format, based in the time zone and format defined as parameters
	 * @param dateValue the date to format
	 * @param tzId the time zone to be used
	 * @param formatType the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param the formatterType defines the format to be used
	 * @return The date formatted
	 **/
	public static String formatDateTimeWithTZ(tzId, formatType, dateValue, String formatterType=FORMAT_DATE_TIME) {
		def formatter = createFormatterForType(formatType, formatterType)
		return formatDateTimeWithTZ(tzId, dateValue, formatter)
	}

	/**
	 * Used to format a Date into a string format, based in the time zone and format defined as parameters
	 * @param dateValue the date to format
	 * @param tzId the time zone to be used
	 * @param the formatter to be used
	 * @return The date formatted
	 **/
	public static String formatDateTimeWithTZ(tzId, dateValue, DateFormat formatter) {
		formatter.setTimeZone(TimeZone.getTimeZone(tzId))
		return formatter.format(dateValue)
	}

	/**
	 * Used to format a Date into a GMT format, using default format type
	 * @param dateValue the date to format
	 * @param the formatterType defines the format to be used
	 * @return The date formatted
	 **/
	public static String formatDateTimeAsGMT(dateValue, String formatterType=FORMAT_DATE_TIME) {
		return formatDateTimeWithTZ(defaultTimeZone, getDefaultFormatType(), dateValue, formatterType)
	}

	/**
	 * Used to parse a string value into a Date, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @return The date
	 **/
	public static Date parseDate(session, dateValue) {
		def formatter = createFormatter(session, FORMAT_DATE)
		def newDate = parseDateTime(session, dateValue, formatter)
		if (newDate) {
			newDate.clearTime()	
		}
		return newDate
	}

	/**
	 * Used to parse a string value into a Date, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @param the formatterType defines the format to be used
	 * @return The date
	 **/
	public static Date parseDateTime(session, dateValue, String formatterType=FORMAT_DATE_TIME) {
		def formatter = createFormatter(session, formatterType)
		return parseDateTime(session, dateValue, formatter)
	}

	/**
	 * Used to parse a string value into a Date, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @param the formatter defines the format to be used
	 * @return The date
	 **/
	public static Date parseDateTime(session, dateValue, DateFormat formatter) {
		def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
		formatter.setTimeZone(TimeZone.getTimeZone(tzId))
		def result
		try {
			result = formatter.parse(dateValue)
		} catch (Exception e) {
			System.out.println("Invalid date: " + e.getMessage())
		}
		return result
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

	/**
	 * Creates a formatter based on the format type defined in the session
	 * @param session
	 * @param formatterType formatter type to be used
	 * @return formatter
	 */
	private static DateFormat createFormatter(session, String formatterType) {
		def type = getDefaultFormatType()
		def userDTFormat = session.getAttribute( DATE_TIME_FORMAT_ATTR )?.CURR_DT_FORMAT
		if (userDTFormat) {
			type = userDTFormat
		}
		return createFormatterForType(type, formatterType)
	}

	/**
	 * Creates a formatter
	 * @param formatType the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param formatterType formatter type to be used
	 * @return formatter
	 */
	private static DateFormat createFormatterForType(String formatType, String formatterType) {
		def formatter
		def isMMDDYYYY = (formatType == getDefaultFormatType())
		switch (formatterType) {
			case FORMAT_DATE:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MM/dd/yyyy")
				else
					formatter = new SimpleDateFormat("dd/MM/yyyy")
				break;
			case FORMAT_DATE_TIME:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
				else
					formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a")
				break;
			case FORMAT_DATE_TIME_2:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a")
				else
					formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a")
				break;
			case FORMAT_DATE_TIME_3:
				formatter = new SimpleDateFormat("E, d MMM 'at ' HH:mma")
				break;
			case FORMAT_DATE_TIME_4:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MM/dd kk:mm")
				else
					formatter = new SimpleDateFormat("dd/MM kk:mm")
				break;
			case FORMAT_DATE_TIME_5:
				formatter = new SimpleDateFormat("yyyyMMdd")
				break;
			case FORMAT_DATE_TIME_6:
				formatter = new SimpleDateFormat("yyyy-MM-dd")
				break;
			case FORMAT_DATE_TIME_7:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("dd-MMM")
				else
					formatter = new SimpleDateFormat("MMM-dd")
				break;
			case FORMAT_DATE_TIME_8:
				formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a")
				break;
			case FORMAT_DATE_TIME_9:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a")
				else
					formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a")
				break;
			case FORMAT_DATE_TIME_10:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MMM dd")
				else
					formatter = new SimpleDateFormat("dd MMM")
				break;
			case FORMAT_DATE_TIME_11:
				formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a")
				break;
			case FORMAT_DATE_TIME_12:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MM-dd-yyyy")
				else
					formatter = new SimpleDateFormat("dd-MM-yyyy")
				break;
			case FORMAT_DATE_TIME_13:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MM/dd kk:mm:ss")
				else
					formatter = new SimpleDateFormat("dd/MM kk:mm:ss")
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

			case FORMAT_DATE_TIME_17:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("MM/dd")
				else
					formatter = new SimpleDateFormat("dd/")
				break;
			case FORMAT_DATE_TIME_18:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("M/d")
				else
					formatter = new SimpleDateFormat("d/M")
				break;
			case FORMAT_DATE_TIME_19:
				if (isMMDDYYYY)
					formatter = new SimpleDateFormat("M/d kk:mm")
				else
					formatter = new SimpleDateFormat("M/d kk:mm")
				break;
		}

		return formatter
	}

}
