package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import java.text.DateFormat
import java.text.SimpleDateFormat
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import javax.servlet.http.HttpSession

/**
 * The TimeUtil class contains a collection of useful Time manipulation methods 
 */
class TimeUtil {

	private static final LOG = LogFactory.getLog(TimeUtil.class)

	//TODO: Remove!!!
	def static timeZones = [GMT:"GMT-00:00", PST:"GMT-08:00", PDT:"GMT-07:00", MST:"GMT-07:00", MDT:"GMT-06:00", 
							CST:"GMT-06:00", CDT:"GMT-05:00", EST:"GMT-05:00",EDT:"GMT-04:00"]

	static final String MIDDLE_ENDIAN = 'MM/DD/YYYY'	// Primarily in the US
	static final String LITTLE_ENDIAN = 'DD/MM/YYYY'	// Used outside US and China
	static final String BIG_ENDIAN    = 'YYYY/MM/DD'	// Used principally in China but not used in TM today

	def static final dateTimeFormatTypes = [MIDDLE_ENDIAN, LITTLE_ENDIAN]

	def static final defaultTimeZone = 'GMT'

	def static final String TIMEZONE_ATTR = 'CURR_TZ'
	def static final String DATE_TIME_FORMAT_ATTR = 'CURR_DT_FORMAT'


	// Valid date time formats
	def static final FORMAT_DATE         = "MM/dd/yyyy"
	def static final FORMAT_DATE_TIME    = "MM/dd/yyyy hh:mm a"
	def static final FORMAT_DATE_TIME_2  = "MM-dd-yyyy hh:mm:ss a"
	def static final FORMAT_DATE_TIME_3  = "E, d MMM 'at ' HH:mma"
	def static final FORMAT_DATE_TIME_4  = "MM/dd kk:mm"
	def static final FORMAT_DATE_TIME_5  = "yyyyMMdd"
	def static final FORMAT_DATE_TIME_6  = "yyyy-MM-dd"
	def static final FORMAT_DATE_TIME_7  = "dd-MMM"
	def static final FORMAT_DATE_TIME_8  = "MMM dd,yyyy hh:mm a"
	def static final FORMAT_DATE_TIME_9  = "MM-dd-yyyy hh:mm a"
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
	def static final FORMAT_DATE_TIME_22 = "MM/dd/yyyy hh:mm:ss a"
	def static final FORMAT_DATE_TIME_23 = "MM/dd/yy"
	def static final FORMAT_DATE_TIME_24 = "MM/dd/yyyy hh:mm:ss"
	def static final FORMAT_DATE_TIME_25 = "MM/dd/yyyy hh:mm"

	static final String SHORT='S'
	static final String FULL='F'
	static final String ABBREVIATED='A'

	public static final String GRANULARITY_SECONDS = "S"
	public static final String GRANULARITY_MINUTES = "M"
	public static final String GRANULARITY_HOURS = "H"
	public static final String GRANULARITY_DAYS = "D"


	/**
	 * Used to get the user perferred timezone
	 * @param session - the HttpSession user session object
	 * @return the timezone id string
	 */
	public static String getUserTimezone(HttpSession session) {
		String tzId = session.getAttribute(TIMEZONE_ATTR)[TIMEZONE_ATTR] ?: defaultTimeZone

		return tzId
	}	 

	/**
	 * Used to get the user perferred timezone
	 * @param session - the HttpSession user session object
	 * @return the timezone id string
	 */
	public static String getUserDateFormat(HttpSession session) {
		return session.getAttribute(DATE_TIME_FORMAT_ATTR)[DATE_TIME_FORMAT_ATTR] ?: MIDDLE_ENDIAN
	}	 

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
	public static TimeDuration elapsed(Date start) {
		def e = 
		start = new Date()
		return elapsed(start, new Date())
	}

	public static TimeDuration elapsed(List startList) {
		def e = elapsed(startList[0], new Date())
		startList[0] = new Date()
		return e
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
	 * Get the datetime in GMT formatted to be used in a sql
	 * @return Date The current date set in GMT in sql format date
	 */
	def public static gmtDateSQLFormat(date) {
		SimpleDateFormat sqlFormatGmt = new SimpleDateFormat("yyyy-MM-dd")
		sqlFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"))
		return sqlFormatGmt.format(date)
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
	 * Used to format a Date into a date string format, based in the format defined in the session
	 * 
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @return The date formatted
	 **/
	public static String formatDate(HttpSession session, Date dateValue) {
		def formatter = createFormatter(session, FORMAT_DATE)
		return formatDateTime(session, dateValue, formatter)
	}

	/**
	 * Used to format a Date into a date string format, based in the format defined in the session
	 * For dates (without time) is not required to applied a timezone.
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @return The date formatted
	 **/
	public static String formatDate(Date dateValue, DateFormat formatter) {
		return formatDateTimeWithTZ('GMT', dateValue, formatter)
	}

	/**
	 * Used to format a Date into a string format, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @param the formatterType defines the format to be used
	 * @return The date formatted
	 **/
	public static String formatDateTime(HttpSession session, dateValue, String formatterType=FORMAT_DATE_TIME) {
		def formatter = createFormatter(session, formatterType)
		def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
		return formatDateTime(tzId, dateValue, formatter)
	}

	/**
	 * Used to format a Date into a string format, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @param the formatter defines the formatter to be used
	 * @return The date formatted
	 **/
	public static String formatDateTime(HttpSession session, dateValue, DateFormat formatter) {
		if (!formatter) {
			// TODO : JPM 4/2016 : formatDateTimeWithTZ should throw InvalidParamException vs RuntimeException (fix below and test cases too)
			// throw new InvalidParamException('formatDateTimeWithTZ called with missing DateFormat formatter parameter')
			throw new RuntimeException('formatDateTimeWithTZ called with missing DateFormat formatter parameter')
		}
		def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
		return formatDateTimeWithTZ(tzId, dateValue, formatter)
	}

	/**
	 * Used to format a Date into a date string format, based in the formatter provided plus the timezone passed
	 * @param tzId - the timezone to render a date for
	 * @param dateValue - the date to format
	 * @param formatter - the formatter to use for the conversion of the date to a string
	 * @return The date formatted as a string
	 * @deprecated formatting a date with a Timezone should not be used as Dates should be handled as an absolute (no time element date)
	 **/
	public static String formatDate(String tzId, Date dateValue, DateFormat formatter) {
		LOG.warn "Deprecated formatDate() called with timezone parameter - should be using formatDate(Date, DateFormat)"
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
	public static String formatDateTimeWithTZ(String tzId, String formatType, dateValue, String formatterType=FORMAT_DATE_TIME) {
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
	public static String formatDateTimeWithTZ(String tzId, dateValue, DateFormat formatter) {
		println "formatDateTimeWithTZ() tzId=$tzId, dateValue=$dateValue, formatter=${formatter ? formatter.toPattern() : null}"
		if (!formatter) {
			// throw new InvalidParamException('formatDateTimeWithTZ called with missing DateFormat formatter parameter')
			throw new RuntimeException('formatDateTimeWithTZ called with missing DateFormat formatter parameter')
		}
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
	 * Used to parse a string value into a Date, based in the format defined in the session.
	 * For dates (without time) is not required to applied a timezone.
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @return The date
	 **/
	public static Date parseDate(session, dateValue) {
		def formatter = createFormatter(session, FORMAT_DATE)
		return parseDate(dateValue, formatter)
	}

	/**
	 * Used to parse a string value into a Date, based in the format defined by formatType.
	 * For dates (without time) is not required to applied a timezone.
	 * @param formatType the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param dateValue the date to format
	 * @param the formatterType defines the format to be used
	 * @return The date
	 **/
	public static Date parseDate(String formatType, String dateValue, String formatterType=FORMAT_DATE) {
		def formatter = createFormatterForType(formatType, formatterType)
		return parseDate(dateValue, formatter)
	}

	/**
	 * Used to parse a string value into a Date using a knowed DateFormat class. If the parse fails the 
	 * method will return a null
	 * @param dateValue - the String value of the date to be parsed
	 * @param formatter - the DateFormat object to use to parse the date
	 * @return The date or null if unparseable
	 **/
	public static Date parseDate(String dateValue, DateFormat formatter) {
		def result
		try {
			result = formatter.parse(dateValue)
			result.clearTime()	
		} catch (Exception e) {
			LOG.warn("parseDate() encountered invalid date ($dateValue): " + e.getMessage(), e)
		}
		return result
	}

	/**
	 * Used to parse a string value into a Date, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @param the formatterType defines the format to be used
	 * @return The date
	 **/
	public static Date parseDateTime(session, dateValue, String formatterType=FORMAT_DATE_TIME) {
		DateFormat formatter = createFormatter(session, formatterType)
		return parseDateTimeWithFormatter(session, dateValue, formatter)
	}

	/**
	 * Used to parse a string value into a Date, based in the time zone and format defined in the session
	 * @param dateValue the date to format
	 * @param session the session information (to get timezone and format type)
	 * @param the formatter defines the format to be used
	 * @return The date
	 **/
	public static Date parseDateTimeWithFormatter(session, dateValue, DateFormat formatter) {
		def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
		return parseDateTimeWithFormatter(tzId, dateValue, formatter)
	}

	/**
	 * Used to parse a string value into a Date, based in the time zone and format defined in the session
	 * @param tzId - the timezone id to parse the datetime value with
	 * @param dateValue - the datetime value to be parsed
	 * @param formatter - the string format that defines the layout of the datetime
	 * @return The date other null
	 **/
	public static Date parseDateTimeWithFormatter(String tzId, dateValue, DateFormat formatter) {
		formatter.setTimeZone(TimeZone.getTimeZone(tzId))
		def result
		try {
			result = formatter.parse(dateValue)
		} catch (Exception e) {
			LOG.warn("Invalid date time: $dateValue, $tzId, ${formatter.toPattern()}")
		}
		return result
	}

	/**
     * This method determines the elapsed time between two dates and
     * returns the value using the given granularity (D|H|M|S).
	 */
	public static Integer elapsed(Date start, Date end, String granularity){
		TimeDuration duration = TimeUtil.elapsed(start, end)
		Integer elapsed = duration.getDays()
		granularity = granularity.toUpperCase()
		if (granularity > GRANULARITY_DAYS){
			elapsed = elapsed * 24 + duration.getHours()
			if(granularity > GRANULARITY_HOURS){
				elapsed = elapsed * 60 + duration.getMinutes()
				if(granularity > GRANULARITY_MINUTES){
					elapsed = elapsed * 60 + duration.getSeconds()
				}

			}
		}
		return elapsed
	}

	private static DateFormat createFormatter(session, String formatterType) {
		def type = getDefaultFormatType()
		def userDTFormat = session.getAttribute( DATE_TIME_FORMAT_ATTR )?.CURR_DT_FORMAT
		if (userDTFormat) {
			type = userDTFormat
		}
		return createFormatterForType(type, formatterType)
	}

	/**
	 * Builder to get a Formatter based on the middle-endian or little-endian, the formatter type, and the tyme zone to use to Parse Dates
	 * @author @tavo_luna
	 * @param @param userPrefFormat - the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param formatterType - the formatter type to be used
	 * @param timezone - timezone to set the formatter
	 * @return formatter - the middle or little-endian version of the format desired
	 */
	public static DateFormat createFormatterForType(String userPrefFormat, String formatterType, String timezone) {
		def formatter = createFormatterForType(userPrefFormat, formatterType)
		formatter?.setTimeZone(TimeZone.getTimeZone(timezone))
		return formatter
	}

	/**
	 * Creates a formatter based on the options presented. Based on the formatType being set to the
	 * user preference of MMDDYYYY or DDMMYYYY then the method will return the equivilent format for 
	 * the middle-endian (US) or little-endian (most other countries)
	 * https://en.wikipedia.org/wiki/Date_format_by_country or all others
	 * @param userPrefFormat - the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param formatterType - the formatter type to be used
	 * @return formatter - the middle or little-endian version of the format desired
	 */
	public static DateFormat createFormatterForType(String userPrefFormat, String formatterType) {
		def formatter
		def isMiddleEndian = (userPrefFormat.toString() == MIDDLE_ENDIAN)
		switch (formatterType) {
			case FORMAT_DATE:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd/yyyy")
				else
					formatter = new SimpleDateFormat("dd/MM/yyyy")
				break
			case FORMAT_DATE_TIME:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
				else
					formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a")
				break
			case FORMAT_DATE_TIME_2:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a")
				else
					formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a")
				break
			case FORMAT_DATE_TIME_3:
				formatter = new SimpleDateFormat("E, d MMM 'at ' HH:mma")
				break
			case FORMAT_DATE_TIME_4:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd kk:mm")
				else
					formatter = new SimpleDateFormat("dd/MM kk:mm")
				break
			case FORMAT_DATE_TIME_5:
				formatter = new SimpleDateFormat("yyyyMMdd")
				break
			case FORMAT_DATE_TIME_6:
				formatter = new SimpleDateFormat("yyyy-MM-dd")
				break
			case FORMAT_DATE_TIME_7:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MMM-dd")
				else
					formatter = new SimpleDateFormat("dd-MMM")
				break
			case FORMAT_DATE_TIME_8:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a")
				else
					formatter = new SimpleDateFormat("dd MMM yyyy hh:mm a")
				break
			case FORMAT_DATE_TIME_9:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a")
				else
					formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a")
				break
			case FORMAT_DATE_TIME_10:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MMM dd")
				else
					formatter = new SimpleDateFormat("dd MMM")
				break
			case FORMAT_DATE_TIME_11:
				formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a")
				break
			case FORMAT_DATE_TIME_12:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM-dd-yyyy")
				else
					formatter = new SimpleDateFormat("dd-MM-yyyy")
				break
			case FORMAT_DATE_TIME_13:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd kk:mm:ss")
				else
					formatter = new SimpleDateFormat("dd/MM kk:mm:ss")
				break
			case FORMAT_DATE_TIME_14:
				formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm")
				break
			case FORMAT_DATE_TIME_15:
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				break
			case FORMAT_DATE_TIME_16:
				formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a")
				break

			case FORMAT_DATE_TIME_17:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd")
				else
					formatter = new SimpleDateFormat("dd/MM")
				break
			case FORMAT_DATE_TIME_18:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("M/d")
				else
					formatter = new SimpleDateFormat("d/M")
				break
			case FORMAT_DATE_TIME_19:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("M/d kk:mm")
				else
					formatter = new SimpleDateFormat("M/d kk:mm")
				break
			case FORMAT_DATE_TIME_20:
				formatter = new SimpleDateFormat("hh:mm")
				break
			case FORMAT_DATE_TIME_21:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("mm/dd")
				else
					formatter = new SimpleDateFormat("dd/mm")
				break
			case FORMAT_DATE_TIME_22:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a")
				else
					formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")
				break
			case FORMAT_DATE_TIME_23:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd/yy")
				else
					formatter = new SimpleDateFormat("dd/MM/yy")
				break
			case FORMAT_DATE_TIME_24:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
				else
					formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
				break
			case FORMAT_DATE_TIME_25:
				if (isMiddleEndian)
					formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm")
				else
					formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm")
				break
		}

		return formatter
	}

	/**
	 * Used to move a Date to GMT which will fetch the user's configured timezone
	 * in order to determine the timezone that the date is currently in.
	 * @param dateValue - the date to move
	 * @param session - the session information (to get timezone and format type)
	 * @return the adjusted date
	 **/
	public static Date moveDateToTZ(dateValue, HttpSession session) {
		def result
		def tzId = session.getAttribute( TIMEZONE_ATTR )?.CURR_TZ
		return moveDateToTZ('GMT', tzId)
	}

	/**
	 * Used to move a Date from GMT to TZ
	 * @param dateValue - the date to move
	 * @param session - the session information (to get timezone and format type)
	 * @return the adjusted date
	 **/
	public static Date moveDateFromGMT(Date date, String toTZ) {
		return moveDateToTZ(date, 'GMT', toTZ)
	}

	/**
	 * Used to adjust a Date from GMT to a specified Timezone
	 * @param dateValue - the date to adjust
	 * @param session - the session information (to get timezone and format type)
	 * @return The adjusted date
	 **/
	public static Date moveDateToGMT(Date date, String toTZ) {
		if (toTZ == 'GMT') {
			return date
		} else {
			return moveDateToTZ(date, 'GMT', toTZ)
		}
	}

	/**
	 * A generic TimeZone Shifter to adjust a date from one timezone to another
	 * @param date - the date to move
	 * @param fromTZ - the timezone that the date was generated in
	 * @param toTZ -  the timezone that the date will be adjusted to
	 * @return the adjusted date
	 * @author @tavo_luna
	 */
	public static Date moveDateToTZ(Date date, String fromTZ, String toTZ){
		def result

		if (fromTZ == toTZ) {
			return date
		}

		if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
			formatter.setTimeZone(TimeZone.getTimeZone(fromTZ))
			String dateFormatted = formatter.format(date)

			formatter.setTimeZone(TimeZone.getTimeZone(toTZ))
			result = formatter.parse(dateFormatted)
		}
		
		return result
	}

}