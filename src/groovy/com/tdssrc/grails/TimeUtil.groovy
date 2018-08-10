package com.tdssrc.grails

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.TimeScale
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.util.logging.Slf4j
import groovy.transform.CompileStatic
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang3.time.DateFormatUtils
import org.springframework.util.Assert

import javax.servlet.http.HttpSession
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * The TimeUtil class contains a collection of useful Time manipulation methods
 */
@Slf4j(value='logger')
class TimeUtil {

	static final String MIDDLE_ENDIAN = 'MM/DD/YYYY'	// Primarily in the US
	static final String LITTLE_ENDIAN = 'DD/MM/YYYY'	// Used outside US and China
	static final String BIG_ENDIAN    = 'YYYY/MM/DD'	// Used principally in China but not used in TM today

	static final List<String> dateTimeFormatTypes = [MIDDLE_ENDIAN, LITTLE_ENDIAN]

	static final String defaultTimeZone = 'GMT'

	static final String TIMEZONE_ATTR = 'CURR_TZ'
	static final String DATE_TIME_FORMAT_ATTR = 'CURR_DT_FORMAT'

	// Valid date time formats
	static final String FORMAT_DATE         = "MM/dd/yyyy"
	static final String FORMAT_DATE_TIME    = "MM/dd/yyyy hh:mm a"
	static final String FORMAT_DATE_TIME_2  = "MM-dd-yyyy hh:mm:ss a"
	static final String FORMAT_DATE_TIME_3  = "E, d MMM 'at ' hh:mm a"
	static final String FORMAT_DATE_TIME_4  = "MM/dd kk:mm"
	static final String FORMAT_DATE_TIME_5  = "yyyyMMdd"
	static final String FORMAT_DATE_TIME_6  = "yyyy-MM-dd"
	static final String FORMAT_DATE_TIME_7  = "dd-MMM"
	static final String FORMAT_DATE_TIME_8  = "MMM dd,yyyy hh:mm a"
	static final String FORMAT_DATE_TIME_9  = "MM-dd-yyyy hh:mm a"
	static final String FORMAT_DATE_TIME_10 = "MMM dd"
	static final String FORMAT_DATE_TIME_11 = "yyyy/MM/dd hh:mm:ss a"
	static final String FORMAT_DATE_TIME_12 = "MM-dd-yyyy"
	static final String FORMAT_DATE_TIME_13 = "MM/dd kk:mm:ss"
	static final String FORMAT_DATE_TIME_14 = "yyyy-MM-dd hh:mm" //Used in queries
	static final String FORMAT_DATE_TIME_15 = "yyyy-MM-dd HH:mm:ss" //Used in queries
	static final String FORMAT_DATE_TIME_16 = "yyyy-MM-dd hh:mm a" //Used in queries
	static final String FORMAT_DATE_TIME_17 = "MM/dd"
	static final String FORMAT_DATE_TIME_18 = "M/d"
	static final String FORMAT_DATE_TIME_19 = "M/d kk:mm"
	static final String FORMAT_DATE_TIME_20 = "hh:mm"
	static final String FORMAT_DATE_TIME_21 = "mm/dd"
	static final String FORMAT_DATE_TIME_22 = "MM/dd/yyyy hh:mm:ss a"
	static final String FORMAT_DATE_TIME_23 = "MM/dd/yy"
	static final String FORMAT_DATE_TIME_24 = "MM/dd/yyyy hh:mm:ss"
	static final String FORMAT_DATE_TIME_25 = "MM/dd/yyyy hh:mm"
	static final String FORMAT_DATE_TIME_26 = "yyyyMMdd_HHmm"

	static final String FORMAT_DATE_TIME_ISO8601 = "yyyy-MM-dd'T'HH:mm'Z'" // Quoted "Z" to indicate UTC, no timezone offset

	static final String SHORT = 'S'
	static final String FULL = 'F'
	static final String ABBREVIATED = 'A'

	public static final String GRANULARITY_SECONDS = "S"
	public static final String GRANULARITY_MINUTES = "M"
	public static final String GRANULARITY_HOURS = "H"
	public static final String GRANULARITY_DAYS = "D"

	/**
	 * Used to adjust a datetime by adding or subtracting a specified number of SECONDS from an existing date
	 * @param date	 a date to be adjusted
	 * @param adjustment  the amount to adjust either positive or negative
	 * @return the adjusted date
	 */
	static Date adjustSeconds(Date date, int adjustment) {
		TimeCategory.getSeconds(adjustment) + date
	}

	/**
	 * Returns the elapsed duration that occured between a start time and now
	 * @param start  starting Datetime
	 * @return TimeDuration
	 */
	static TimeDuration elapsed(Date start) {
		elapsed(start, new Date())
	}

	/**
	 * This is Supposed to get the Elapsed Time until now from a given date wrapped in a list for later updating this Date.
	 * I dont recomend this approach since its Error prone and Mutability es the root of all evil :) rather change it in the caller.
	 *
	 * @deprecated Use {@link com.tdssrc.grails.StopWatch} or {@link org.apache.commons.lang.time.StopWatch}
	 */
	static TimeDuration elapsed(List<Date> startList) {
		TimeDuration e = elapsed(startList[0], new Date())
		startList[0] = new Date()
		e
	}

	/**
	 * Returns the elapsed duration that occured between two date objects as a TimeDuration.
	 * @param start  starting Datetime
	 * @param end  ending datetime
	 * @return the time delta
	 */
	static TimeDuration elapsed(Date	start, Date	end) {
		if (!start || !end) {
			new TimeDuration(0, 0, 0, 0)
		}
		else {
			TimeCategory.minus end, start
		}
	}

	/**
	 * Returns a string that represents the time in shorthand that follows these rules:
	 * > 24 hours - ##d ##h ##s
	 * > 1 hour - ##h ##m ##s
	 * <= 90 minutes - ##m ##s
	 * >1 minute - ##s
	 * @param duration  The elapsed duration
	 * @param format - the format of the time units (SHORT- h/m/s, ABBREVIATED hr/min/sec, FULL hour/minute/second) default SHORT
	 * @return The time formatted
	 * @examples
	 *     3d 4h 10m
	 *     3-days 4-hrs 10-min
	 *     10-min 5-sec
	 *     3-days 4-hours 10-minutes
	 */
	static String ago(TimeDuration duration, String format = SHORT) {
		StringBuilder ago = new StringBuilder()
		String space

		int days = duration.days
		int hours = duration.hours
		int minutes = duration.minutes
		int seconds = duration.seconds

		// local closure to do the borrowing math on the d:h:m:s appropriately
		def adjustForNegative = { ggrand, grand, parent, value, factor ->
			if (value < 0) {
				if (ggrand == 0 && grand == 0 && parent == 0) {
					// We stop borrowing and just flip the value from negative to positive
					value *= -1
				}
				else {
					// Borrow from the parent
					int adj = value.intdiv(factor) + 1
					value = factor + value
					if (parent < 0) {
						parent += adj
					}
					else {
						parent -= adj
					}
				}
			}
			else {
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

			ago << '-'
		}

		if (days != 0) {
			ago          << days    << (format == SHORT ? 'd' : '-day' + (days == 1 ? '' : 's'))
		}
		// Hours
		if (hours != 0) {
			space = ago.length() > 1 ? ' ' : ''
			ago << space << hours   << (format == SHORT ? 'h' : (format == FULL ? '-hour' : '-hr') + (hours == 1 ? '' : 's'))
		}
		// Minutes
		if (days == 0 && minutes > 0) {
			space = ago.length() > 1 ? ' ' : ''
			ago << space << minutes << (format == SHORT ? 'm' : (format == FULL ? '-minute' : '-min' ) + (minutes == 1 ? '' : 's'))
		}
		// Only show seconds if day/hr are zero
		if (days == 0 && hours == 0 && seconds > 0) {
			space = ago.length() > 1 ? ' ' : ''
			ago << space << seconds << (format == SHORT ? 's' : (format == FULL ? '-second' : '-sec' ) + (seconds == 1 ? '' : 's'))
		}

		ago.toString()
	}

	/**
	 * Overload variation of the ago method that accepts an integer in seconds
	 * @param secs - the number of seconds
	 * @return The time in shorthand
	 */
	static String ago(int secs, String format = SHORT) {
		Date base = new Date(0)
		ago(base, TimeCategory.getSeconds(secs) + base, format)
	}

	/**
	 * Overloaded variation of the ago(TimeDuration) method that accepts a start and ending time
	 * @param start  a starting datetime
	 * @param end  an ending datetime
	 * @return The elapsed time in shorthand
	 */
	static String ago(Date start, Date end, String format = SHORT) {
		ago(elapsed(start, end), format)
	}

	/**
	 * @return the current datetime in GMT
	 */
	static Date nowGMT() {
		new Date()
	}

	/**
	 * @return  the current datetime in GMT in SQL format
	 */
	static String nowGMTSQLFormat() {
		gmtDateSQLFormat new Date()
	}

	/**
	 * @return  the specified date in GMT in SQL format
	 */
	static String gmtDateSQLFormat(Date date) {
		formatDateTimeWithTZ defaultTimeZone, date, new SimpleDateFormat(FORMAT_DATE_TIME_6)
	}

	/**
	 * Check if value is a valid format type and if not returns default
	 * @return The format type
	 */
	static String getDateTimeFormatType(String value) {
		if (value in dateTimeFormatTypes) {
			value
		}
		else {
			getDefaultFormatType()
		}
	}

	/**
	 * Default format type
	 * @return The default format type
	 */
	static String getDefaultFormatType() {
		dateTimeFormatTypes[0]
	}

	/**
	 * Format a Date to a string with the user's default format.
	 *
	 * @param date  the date to format
	 * @return The date formatted
	 */
	static String formatDate(Date date) {
		if (!date) return ''
		formatDate(date, createFormatter(FORMAT_DATE))
	}

	/**
	 * Format a Date to a string with the specified formatter.
	 * For dates (without time) is not required to applied a timezone.
	 * @param date  the date to format
	 * @return The date formatted
	 */
	static String formatDate(Date date, DateFormat formatter) {
		if (!date) return ''
		formatDateTimeWithTZ(defaultTimeZone, date, formatter)
	}

	/**
	 * Used to format a Date into a date string format, based in the format defined in the param
	 * For dates (without time) is not required to applied a timezone.
	 * @param date  the date to format
	 * @param formatType the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param formatterType defines the format to be used
	 * @return The date formatted
	 */
	static String formatDate(String formatType, Date date, String formatterType = FORMAT_DATE_TIME) {
		if (!date) return ''
		formatDate(date, createFormatterForType(formatType, formatterType))
	}

	/**
	 * Used to format a Date into a date string format, based in the formatter provided plus the timezone passed
	 * @param tzId - the timezone to render a date for
	 * @param date - the date to format
	 * @param formatter - the formatter to use for the conversion of the date to a string
	 * @return The date formatted as a string
	 * @deprecated formatting a date with a Timezone should not be used as Dates should be handled as an absolute (no time element date)
	 */
	static String formatDate(String tzId, Date date, DateFormat formatter) {
		logger.warn 'Deprecated formatDate() called with timezone parameter - should be using formatDate(Date, DateFormat)'
		if (!date) return ''
		formatDateTimeWithTZ(tzId, date, formatter)
	}

	/**
	 * Format a Date to a string with the user's time zone and date format.
	 * @param date the date to format
	 * @param the formatterType defines the format to be used
	 * @return The date formatted
	 */
	static String formatDateTime(Date date, String formatterType = FORMAT_DATE_TIME) {
		if (!date) return ''
		formatDateTime(StringUtil.defaultIfEmpty(userPreferenceService.timeZone, defaultTimeZone), date, createFormatter(formatterType))
	}

	/**
	 * Format a Date to a string with the user's time zone and date format (using a Long EPOC Date representation).
	 * @param ldate long date representation (EPOC)
	 */
	static String formatDateTime(Long ldate, String formatterType = FORMAT_DATE_TIME) {
		if (!ldate) return ''
		formatDateTime(new Date(ldate), formatterType)
	}

	/**
	 * Format a Date to a string with the specified formatter and the user's default time zone.
	 * @param date the date to format
	 * @param the formatter defines the formatter to be used
	 * @return The date formatted
	 */
	static String formatDateTime(Date date, DateFormat formatter) {
		if (!formatter) {
			// TODO : JPM 4/2016 : formatDateTimeWithTZ should throw InvalidParamException vs RuntimeException (fix below and test cases too)
			// throw new InvalidParamException('formatDateTimeWithTZ called with missing DateFormat formatter parameter')
			throw new RuntimeException('formatDateTime called with missing DateFormat formatter parameter')
		}
		if (!date) return ''
		formatDateTimeWithTZ(userPreferenceService.timeZone ?: defaultTimeZone, date, formatter)
	}

	/**
	 * Used to format a Date into a datetime string format, based in the formatter provided plus the timezone passed
	 * @param tzId - the timezone to render a date for
	 * @param date - the date to format
	 * @param formatter - the formatter to use for the conversion of the date to a string
	 * @return The date formatted as a string
	 * @deprecated formatting a date with a Timezone should not be used as Dates should be handled as an absolute (no time element date)
	 */
	static String formatDateTime(String tzId, Date date, DateFormat formatter) {
		if (!date) return ''
		formatDateTimeWithTZ(tzId, date, formatter)
	}

	static String formatDateTime(String tzId, long time, DateFormat formatter) {
		formatDateTimeWithTZ(tzId, new Timestamp(time), formatter)
	}

	/**
	 * Used to format a Date into a string format, based in the time zone and format defined as parameter
	 * @param date  the date to format
	 * @param tzId the time zone to be used
	 * @param formatType the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param the formatterType defines the format to be used
	 * @return The date formatted
	 */
	static String formatDateTimeWithTZ(String tzId, String formatType, Date date, String formatterType = FORMAT_DATE_TIME) {
		if (!date) return ''
		formatDateTimeWithTZ(tzId, date, createFormatterForType(formatType, formatterType))
	}

	/**
	 * Used to format a Date into a string format, based in the time zone and format defined as parameters
	 * @param date the date to format
	 * @param tzId the time zone to be used
	 * @param the formatter to be used
	 * @return The date formatted
	 */
	static String formatDateTimeWithTZ(String tzId, Date date, DateFormat formatter) {
		if (!date) return ''
		Assert.notNull formatter, 'formatDateTimeWithTZ called with missing DateFormat formatter parameter'

		if(!tzId){
			tzId = TimeUtil.getDefaultTimeZoneId()
		}

		formatter.setTimeZone(TimeZone.getTimeZone(tzId))
		formatter.format(date)
	}

	/**
	 * Used to format a Date into a GMT format, using default format type
	 * @param date  the date to format
	 * @param formatterType  the formatterType defines the format to be used
	 * @return The date formatted
	 */
	static String formatDateTimeAsGMT(Date date, String formatterType = FORMAT_DATE_TIME) {
		if (!date) return ''
		formatDateTimeWithTZ(defaultTimeZone, getDefaultFormatType(), date, formatterType)
	}

	/**
	 * Used by Import processes to get a string representation for the given date
	 * (String or Date instance) using the formatter provided.
	 *
	 * @param date - string or date instance.
	 * @param formatter - date or datetime formatter
	 * @return string representation for the given date properly formatted.
	 */
	static String dateToStringFormat(Object date, SimpleDateFormat formatter) {
		if (date == null) {
			return ''
		}

		Date formattedDate = null

		if (date instanceof String) {
			formattedDate = parseDate(date, formatter)
			if (! formattedDate) {
				logger.error ("xfrmDateToString() cannot parse date '{}' using pattern: '{}'",
						date, formatter.toPattern())
			}

		} else if (date instanceof Date) {
			formattedDate = date

		} else {
			logger.error ("xfrmDateToString() got unexpected data type {}", date.getClass().getName())

		}

		if (formattedDate) {
			return formatDate(formattedDate, formatter)
		}

		return date.toString()
	}

	/**
	 * Parse a string value to a Date with the user's default format.
	 * For dates (without time) is not required to applied a timezone.
	 * @param dateString the date to format
	 * @return The date
	 */
	static Date parseDate(String dateString) {
		if (StringUtil.isBlank(dateString)) return null
		parseDate(dateString, createFormatter(FORMAT_DATE))
	}

	/**
	 * Used to parse a string value into a Date, based in the format defined by formatType.
	 * For dates (without time) is not required to applied a timezone.
	 * @param formatType the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param dateString the date to format
	 * @param the formatterType defines the format to be used
	 * @return The date
	 */
	static Date parseDate(String formatType, String dateString, String formatterType=FORMAT_DATE) {
		if (StringUtil.isBlank(dateString)) return null
		parseDate(dateString, createFormatterForType(formatType, formatterType))
	}

	/**
	 * Used to parse a string value into a Date using a knowed DateFormat class. If the parse fails the
	 * method will return a null
	 * @param dateString - the String value of the date to be parsed
	 * @param formatter - the DateFormat object to use to parse the date
	 * @return The date or null if unparseable
	 */
	static Date parseDate(String dateString, DateFormat formatter) {
		if (StringUtil.isBlank(dateString)) return null
		try {
			Date result = formatter.parse(dateString)
			result.clearTime()
			return result
		}
		catch (e) {
			logger.debug "parseDate() encountered invalid date ({}) format '{}' : {}",
					dateString, formatter?.toPattern(), e.message, e
		}
	}

	/**
	 * Parse a string to a Date with the user's default time zone and format type.
	 * @param dateString the date to format
	 * @param the formatterType defines the format to be used
	 * @return the Date or null if there's a problem
	 */
	static Date parseDateTime(String dateString, String formatterType = FORMAT_DATE_TIME) {
		if (StringUtil.isBlank(dateString)) return null
		parseDateTimeWithFormatter(userPreferenceService.timeZone, dateString, createFormatter(formatterType))
	}

	/**
	 * Parse a string to a Date using the specified formatter.
	 * @param tzId - the timezone id to parse the datetime value with
	 * @param dateString - the string to be parsed
	 * @param formatter - the string format that defines the layout of the datetime
	 * @return the Date or null if there's a problem
	 */
	static Date parseDateTimeWithFormatter(String tzId, String dateString, DateFormat formatter) {
		if (StringUtil.isBlank(dateString)) return null
		formatter.setTimeZone(TimeZone.getTimeZone(tzId))
		try {
			return formatter.parse(dateString)
		}
		catch (e) {
			logger.debug 'Invalid date time: {}, {}, {}', dateString, tzId, formatter.toPattern()
		}
	}

	/**
	 * This method determines the elapsed time between two dates and
	 * returns the value using the given granularity (D|H|M|S).
	 */
	static int elapsed(Date start, Date end, String granularity) {
		TimeDuration duration = elapsed(start, end)
		int elapsed = duration.days
		granularity = granularity.toUpperCase()
		if (granularity > GRANULARITY_DAYS) {
			elapsed = elapsed * 24 + duration.hours
			if (granularity > GRANULARITY_HOURS) {
				elapsed = elapsed * 60 + duration.minutes
				if (granularity > GRANULARITY_MINUTES) {
					elapsed = elapsed * 60 + duration.seconds
				}
			}
		}
		return elapsed
	}

	static DateFormat createFormatter(String formatterType) {
		createFormatterForType(userPreferenceService.dateFormat, formatterType)
	}

	/**
	 * Creates a formatter based on the options presented. Based on the formatType being set to the
	 * user preference of MMDDYYYY or DDMMYYYY then the method will return the equivilent format for
	 * the middle-endian (US) or little-endian (most other countries)
	 * https://en.wikipedia.org/wiki/Date_format_by_country or all others
	 * @param userPrefFormat - the format type to be used, valid values defined in dateTimeFormatTypes
	 * @param formatterType - the formatter type to be used
	 * @param timezone  optional timezone to set the formatter
	 * @return formatter - the middle or little-endian version of the format desired
	 */
	static DateFormat createFormatterForType(String userPrefFormat, String formatterType, String timezone = null) {
		String format
		boolean isMiddleEndian = userPrefFormat == MIDDLE_ENDIAN
		switch (formatterType) {
			case FORMAT_DATE:
				format = isMiddleEndian ? FORMAT_DATE : "dd/MM/yyyy"
				break
			case FORMAT_DATE_TIME:
				format = isMiddleEndian ? FORMAT_DATE_TIME : "dd/MM/yyyy hh:mm a"
				break
			case FORMAT_DATE_TIME_2:
				format = isMiddleEndian ? FORMAT_DATE_TIME_2 : "dd-MM-yyyy hh:mm:ss a"
				break
			case FORMAT_DATE_TIME_3:
				format = FORMAT_DATE_TIME_3
				break
			case FORMAT_DATE_TIME_4:
				format = isMiddleEndian ? FORMAT_DATE_TIME_4 : "dd/MM kk:mm"
				break
			case FORMAT_DATE_TIME_5:
				format = FORMAT_DATE_TIME_5
				break
			case FORMAT_DATE_TIME_6:
				format = FORMAT_DATE_TIME_6
				break
			case FORMAT_DATE_TIME_7:
				format = isMiddleEndian ? "MMM-dd" : FORMAT_DATE_TIME_7
				break
			case FORMAT_DATE_TIME_8:
				format = isMiddleEndian ? FORMAT_DATE_TIME_8 : "dd MMM yyyy hh:mm a"
				break
			case FORMAT_DATE_TIME_9:
				format = isMiddleEndian ? FORMAT_DATE_TIME_9 : "dd-MM-yyyy hh:mm a"
				break
			case FORMAT_DATE_TIME_10:
				format = isMiddleEndian ? FORMAT_DATE_TIME_10 : "dd MMM"
				break
			case FORMAT_DATE_TIME_11:
				format = FORMAT_DATE_TIME_11
				break
			case FORMAT_DATE_TIME_12:
				format = isMiddleEndian ? FORMAT_DATE_TIME_12 : "dd-MM-yyyy"
				break
			case FORMAT_DATE_TIME_13:
				format = isMiddleEndian ? FORMAT_DATE_TIME_13 : "dd/MM kk:mm:ss"
				break
			case FORMAT_DATE_TIME_14:
				format = FORMAT_DATE_TIME_14
				break
			case FORMAT_DATE_TIME_15:
				format = FORMAT_DATE_TIME_15
				break
			case FORMAT_DATE_TIME_16:
				format = FORMAT_DATE_TIME_16
				break
			case FORMAT_DATE_TIME_17:
				format = isMiddleEndian ? FORMAT_DATE_TIME_17 : "dd/MM"
				break
			case FORMAT_DATE_TIME_18:
				format = isMiddleEndian ? FORMAT_DATE_TIME_18 : "d/M"
				break
			case FORMAT_DATE_TIME_19:
				format = FORMAT_DATE_TIME_19
				break
			case FORMAT_DATE_TIME_20:
				format = FORMAT_DATE_TIME_20
				break
			case FORMAT_DATE_TIME_21:
				format = isMiddleEndian ? FORMAT_DATE_TIME_21 : "dd/mm"
				break
			case FORMAT_DATE_TIME_22:
				format = isMiddleEndian ? FORMAT_DATE_TIME_22 : "dd/MM/yyyy hh:mm:ss a"
				break
			case FORMAT_DATE_TIME_23:
				format = isMiddleEndian ? FORMAT_DATE_TIME_23 : "dd/MM/yy"
				break
			case FORMAT_DATE_TIME_24:
				format = isMiddleEndian ? FORMAT_DATE_TIME_24 : "dd/MM/yyyy hh:mm:ss"
				break
			case FORMAT_DATE_TIME_25:
				format = isMiddleEndian ? FORMAT_DATE_TIME_25 : "dd/MM/yyyy hh:mm"
				break
			case FORMAT_DATE_TIME_26:
				format = FORMAT_DATE_TIME_26
				break
		}

		if (!format) {
			return null
		}

		DateFormat formatter = new SimpleDateFormat(format)
		if (timezone) {
			formatter.timeZone = TimeZone.getTimeZone(timezone)
		}
		formatter
	}

	/**
	 * Used to adjust a Date from a specified Timezone to GMT
	 * @param date  the date to adjust
	 * @param fromTZ - the timezone that the date was generated in
	 * @return The date Timezone
	 */
	static Date moveDateToGMT(Date date, String fromTZ) {
		if (fromTZ == defaultTimeZone) {
			date
		}
		else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date)
			TimeZone fromTimeZone = TimeZone.getTimeZone(fromTZ);

			// Apply the offset to move time to UTC
			calendar.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1)

			if (fromTimeZone.inDaylightTime(calendar.getTime())) { // Apply time saving offset, if it exsist
				calendar.add(Calendar.MILLISECOND, calendar.getTimeZone().getDSTSavings() * -1);
			}
			int year = calendar.get(Calendar.YEAR)
			int month = calendar.get(Calendar.MONTH) + 1 // Calendar indexes months from 0-11 and not 1-12 as DateFormat, so we need to add 1 to construct a Date
			int day = calendar.get(Calendar.DATE)
			int hour = calendar.get(Calendar.HOUR_OF_DAY)
			int minutes = calendar.get(Calendar.MINUTE)
			SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME_25); // "MM/dd/yyyy hh:mm"
			Date result = sdf.parse(month + '/' + day + '/' + year + ' ' + hour + ':' + minutes)
			return result
		}
	}

	/**
	 * Used to adjust a Date from GMT to a specified Timezone
	 * @param date - the date to move
	 * @param toTZ -  the timezone that the date will be adjusted to
	 * @return the adjusted date
	 */
	@CompileStatic
	private static Date adjustDateFromGMTToTZ(Date date, String toTZ) {
		if (toTZ == defaultTimeZone) {
			date
		} else {
			Calendar calendar = Calendar.getInstance()
			calendar.setTime(date)
			TimeZone toTimeZone = TimeZone.getTimeZone(toTZ)

			// Apply the offset to move GMT time to Timezone
			calendar.add(Calendar.MILLISECOND, toTimeZone.getRawOffset())
			if (toTimeZone.inDaylightTime(new Date())) { // Apply time saving offset, if it exists
				calendar.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings())
			}

			int year = calendar.get(Calendar.YEAR)
			int month = calendar.get(Calendar.MONTH) + 1 // Calendar indexes months from 0-11 and not 1-12 as DateFormat, so we need to add 1 to construct a Date
			int day = calendar.get(Calendar.DATE)
			int hour = calendar.get(Calendar.HOUR_OF_DAY)
			int minutes = calendar.get(Calendar.MINUTE)
			SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME_25); // "MM/dd/yyyy hh:mm"
			Date result = sdf.parse(month + '/' + day + '/' + year + ' ' + hour + ':' + minutes)
			return result
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
	private static Date moveDateToTZ(Date date, String fromTZ, String toTZ) {
		if (date == null || fromTZ == toTZ) {
			return date
		}

		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_DATE_TIME_15)
		formatter.setTimeZone(TimeZone.getTimeZone(fromTZ))
		String dateFormatted = formatter.format(date)

		formatter.setTimeZone(TimeZone.getTimeZone(toTZ))
		formatter.parse(dateFormatted)
	}

	/**
	 * A TimeZone Shifter to adjust a date to the User Preference Timezone
	 * @param date - the date to move
	 * @return the adjusted date
	 */
	private static Date moveDateToUserTZ(Date date) {

		if (date == null) {
			return date
		}
		String userTimezone = userPreferenceService.timeZone
		SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_DATE_TIME_15)
		String localTimezone = formatter.getTimeZone().getID()

		formatter.setTimeZone(TimeZone.getTimeZone(userTimezone))
		String dateFormatted = formatter.format(date)

		formatter.setTimeZone(TimeZone.getTimeZone(localTimezone))
		Date result = formatter.parse(dateFormatted)
		return result
	}


	/**
	 * Creates a properly denominated TimeDuration object for the given time quantities
	 * @param seconds - the number of seconds in the duration
	 * @param minues - the number of minutes in the duration
	 * @param hours - the number of hours in the duration
	 * @param days - the number of days in the duration
	 * @return TimeDuration object representing the combination of the given time denominations
	 */
	static TimeDuration createProperDuration(long days = 0, long hours = 0, long minutes = 0, long seconds = 0) {
		minutes += seconds / 60
		hours += minutes / 60
		days += hours / 24
		seconds %= 60
		minutes %= 60
		hours %= 24

		new TimeDuration((int)days, (int)hours, (int)minutes, (int)seconds, 0)
	}

	static TimeDuration createProperDuration(TimeDuration duration) {
		createProperDuration duration.days, duration.hours, duration.minutes, duration.seconds
	}

	private static UserPreferenceService getUserPreferenceService() {
		ApplicationContextHolder.getBean('userPreferenceService', UserPreferenceService)
	}

	/**
	 * Formats a TimeDuration instance to a 2-digits colon-separated string
	 * with the corresponding values. The implementation will automatically
	 * include days, minutes and second. However, seconds and milliseconds can
	 * also be included using the optional parameters.
	 *
	 * @param timeDuration: TimeDuration instance (passing null will return
	 *					"00:00:00")
	 * @param includeSeconds: boolean flag to indicate whether seconds should
	 *					be included or not. Defaulted to false.
	 * @param includeMillis: boolean flag to indicate whether millis should be
	 *					included or not. Defaulted to false.
	 * @return String of the form XX:XX:XX, XX:XX:XX:XX:XX
	 */
	static String formatTimeDuration(TimeDuration timeDuration, boolean includeSeconds=false, boolean includeMillis=false){

		def fields = ["days", "hours", "minutes"]

		if(includeSeconds){
			fields << "seconds"
			// Millis will only be considered if seconds is also set to true.
			if(includeMillis){
				fields << "millis"
			}
		}

		def formatted = []
		if(timeDuration){
			fields.each{ field ->
				int value = timeDuration."$field"
				String valueStr = value > 9 ? value : "0" + value
				formatted << valueStr
			}
		}else{
			fields.each{
				formatted << "00"
			}
		}

		return formatted.join(":")

	}


	private static String getFromHttpSession(HttpSession session, String key, String defaultValue = null) {
		def value = session?.getAttribute(key)
		/*
		//The following code is a failsafe due to a change in the preferences (TM-5572) should never be called
		if(value instanceof Map){
			value = value[key]
		}
		*/
		return value ?: defaultValue
	}

	/**
	 * Used to get the user perferred timezone
	 * @param session - the HttpSession user session object
	 * @return the timezone id string
	 */
	static String getUserTimezone(HttpSession session) {
		return TimeUtil.getFromHttpSession(session, TIMEZONE_ATTR, defaultTimeZone)
	}

	/**
	 * Used to get the user perferred timezone
	 * @param session - the HttpSession user session object
	 * @return the timezone id string
	 */
	static String getUserDateFormat(HttpSession session) {
		return TimeUtil.getFromHttpSession(session, DATE_TIME_FORMAT_ATTR, getDefaultFormatType())
	}


	/**
	 * This method should be called when the user has no Time Zone Id
	 * it his preferences.
	 */
	static String getDefaultTimeZoneId(){
		return 'GMT'
	}

	/**
	 * Used to convert a datetime to minutes
	 * @param date - the date to convert to minutes
	 * @return a date in minutes or null if parameter is null
	 */
	static Integer timeInMinutes(Date datetime) {
		datetime ? datetime.getTime() / 1000 / 60 : null
	}

	/**
	 * Formatting a Date to UTC and the standard ISO8601 date format
	 * @param date
	 * @return String ISO8601
	 */
	static String formatToISO8601DateTime(Date date) {
		DateFormatUtils.formatUTC(date, FORMAT_DATE_TIME_ISO8601)
	}

	/**
	 * Provide a function for formatting durations with the same logic the front-end has.
	 * This method expresses a TimeDuration in days, hours and minutes and allows for negative durations. This is
	 * to support scenarios such as the delta between the Task Estimated Duration and the Actual Duration.
	 *
	 * Examples:
	 *      3 days 1 minute
	 *      1 day 10 hours
	 *      20 hours
	 *      -1 day 1 hour
	 *
	 * @param duration
	 * @return
	 */
	static String formatDuration(TimeDuration duration) {
		// If the parameter is null, return null.
		if (duration == null) {
			return null
		}

		// Fields of a Time Duration used for formatting.
		String[] labels = ["day", "hr", "min"]
		// Field values for the given Time Duration.
		int[] values = [duration.days, duration.hours, duration.minutes]
		// List that will contain each field formatted accordingly.
		List<String> formattedValues = []
		// Iterate over each field formatting it if greater than zero.
		for (int i = 0; i < labels.length; i++) {
			int currentValue = values[i]
			// If the current value is greater than zero, it needs to be included and formatted.
			if (currentValue != 0) {
				String currentLabel = labels[i]
				String formattedValue = "$currentValue $currentLabel"
				// If the value is greater than one, then add an 's' to the label.
				if (Math.abs(currentValue) > 1) {
					formattedValue = formattedValue + "s"
				}
				// Add the current formatted value to the list of formatted fields.
				formattedValues << formattedValue
			}
		}

		return formattedValues.join(" ")
	}

	/**
	 * Create a TimeDuration from a duration and a TimeScale. This is useful
	 * when working with tasks, since its duration is stored as a pair duration, scale to a
	 * format that can be easily manipulated.
	 *
	 * @param duration
	 * @param scale
	 * @return
	 */
	static TimeDuration createTimeDuration(Integer duration, TimeScale scale) {
		if (duration == null || scale == null) {
			return null
		}
		int days = 0
		int hours = 0
		int minutes = 0
		switch (scale) {
			case TimeScale.W:
				days = duration * 7
				break
			case TimeScale.D:
				days = duration
				break
			case TimeScale.H:
				hours = duration % 24
				days = duration / 24
				break
			case TimeScale.M:
				minutes = duration % 60
				hours = (duration % 1440) / 60
				days = duration / 1440
				break
		}

		return new TimeDuration(days, hours, minutes, 0, 0)
	}
}
