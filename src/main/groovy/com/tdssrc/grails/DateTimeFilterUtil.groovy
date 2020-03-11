package com.tdssrc.grails

import grails.util.Pair
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.exception.InvalidParamException
import org.joda.time.DateTime
import org.joda.time.IllegalFieldValueException
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

@Slf4j
@CompileStatic
class DateTimeFilterUtil {

	static Pair<Date, Date> parseUserEntry(String entry) {
		Pair<Date, Date> result = get(entry)
		log.debug('Result for user entry: {} ==> } {}', entry, result)
		return result
	}

	private static Pair<Date, Date> get(String entry) {
		log.debug('Getting Date/DateTime filter value for: {}', entry)
		switch (entry) {
			case ~/^=?\d{4}$/:
				return getFullYear(entry)
			case ~/^=?\d{4}-(0[1-9]|1[012])$/:
				return getFullYearMonth(entry)
			case ~/^=?\d{4}-(0[1-9]|1[012])-(0[1-9]|[1-2][0-9]|3[01])$/:
				return getFullYearMonthDay(entry)
			case ~/^=?\d{4}-(0[1-9]|1[012])-(0[1-9]|[1-2][0-9]|3[01])T(([0-5][0-9]):){2}([0-5][0-9])\.?\d{3}Z$/:
				return getFullYearMonthDayOffset(entry)
			case ~/^=?[0-9\-]*<>=?[0-9\-]*$/:
				return getDateRange(entry)
			case ~/^[-+]?[0-9]*[dwM]?<>[-+]?[0-9]*[dwM]?$/:
				return getDateRange(entry)
			case ~/^0|t$/:
				return getToday()
			case ~/^[-+]?\d{1,3}d?$/:
				return getDayRange(entry)
			case ~/^[-+]?\d+w$/:
				return getWeekRange(entry)
			case ~/^[-+]?\d+M$/:
				return getMonthRange(entry)
			default:
				throw new InvalidParamException('Invalid user entry for Date/DateTime filter: ' + entry)
		}

	}

	private static Pair<Date, Date> getFullYear(String entry) {
		String year = entry.replace(/=/, '')
		return new Pair(getFirstInstantOf(year, null, null), getLastInstantOf(year, null, null))
	}

	private static Date getFirstInstantOf(String year, String month, String day) {
		int y = Integer.valueOf(year)
		int m = month == null ? 1 : Integer.valueOf(month)
		int d = day == null ? 1 : Integer.valueOf(day)
		try {
			return new DateTime(y, m, d, 0, 0).toDate()
		} catch (IllegalFieldValueException e) {
			log.info('Error parsing date. {}', e.message)
			// typical error when testing last day of February
			throw new InvalidParamException('Invalid user entry for Date/DateTime day filter: ' + day)
		}
	}

	private static Date getLastInstantOf(String year, String month, String day) {
		int y = Integer.valueOf(year)
		int m = month == null ? 12 : Integer.valueOf(month)
		int d = day == null ? 1 : Integer.valueOf(day)
		// if day is not given, it must be determined
		if (!day) {
			try {
				LocalDate lastDayOfGivenMonth = new LocalDate(y, m, d)
				d = lastDayOfGivenMonth.dayOfMonth().getMaximumValue()
			} catch (IllegalFieldValueException e) {
				log.info('Error parsing date. {}', e.message)
				// typical error when testing last day of February
				throw new InvalidParamException('Invalid user entry for Date/DateTime day filter: ' + day)
			}
		}
		return new DateTime(y, m, d,23, 59, 59).toDate()
	}

	private static Pair<Date, Date> getFullYearMonth(String entry) {
		String sanitizedEntry = entry.replace(/=/, '')
		String[] sanitizedEntryParts = sanitizedEntry.split('-')
		String year = sanitizedEntryParts[0]
		String month = sanitizedEntryParts[1]
		return new Pair(getFirstInstantOf(year, month, null), getLastInstantOf(year, month, null))
	}

	private static Pair<Date, Date> getFullYearMonthDay(String entry) {
		String sanitizedEntry = entry.replace(/=/, '')
		String[] sanitizedEntryParts = sanitizedEntry.split('-')
		String year = sanitizedEntryParts[0]
		String month = sanitizedEntryParts[1]
		String day = sanitizedEntryParts[2]
		return new Pair(getFirstInstantOf(year, month, day), getLastInstantOf(year, month, day))
	}

	private static Pair<Date, Date> getFullYearMonthDayOffset(String entry) {
		def timezoneName = TimeUtil.parseISO8601Date(entry).toString().substring(20, 23)
		def result = getFullYearMonthDay(entry.substring(0, 10))
		return new Pair(TimeUtil.adjustDateFromGMTToTZ(result.getaValue(), timezoneName), TimeUtil.adjustDateFromGMTToTZ(result.getbValue(), timezoneName))
	}

	private static Pair<Date, Date> getDateRange(String entry) {
		String sanitizedEntry = entry.replace(/=/, '')
		String[] sanitizedEntryParts = sanitizedEntry.split('<>')
		String lowPart = sanitizedEntryParts[0]
		String highPart = sanitizedEntryParts[1]
		Date lowDate = get(lowPart).getaValue()
		Date highDate = get(highPart).getbValue()
		if (lowDate.after(highDate)) {
			throw new InvalidParamException('Lower date range cannot be greater than higher date range: ' + entry)
		}
		return new Pair(lowDate, highDate)
	}

	private static Pair<Date, Date> getToday() {
		DateTime lowDate = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0)
		DateTime highDate = new DateTime().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59)
		return new Pair(lowDate.toDate(), highDate.toDate())
	}

	private static Pair<Date, Date> getDayRange(String entry) {
		String sanitizedEntry = entry.replaceAll(/[+d]/, '')
		int days = Integer.valueOf(sanitizedEntry)
		Pair<Date, Date> today = getToday()
		DateTime lowDate = new DateTime(today.getaValue())
		DateTime highDate = new DateTime(today.getbValue())
		if (days < 0) {
			lowDate = lowDate.minusDays(days * -1)
		} else {
			highDate = highDate.plusDays(days)
		}
		return new Pair(lowDate.toDate(), highDate.toDate())
	}

	private static Pair<Date, Date> getWeekRange(String entry) {
		String sanitizedEntry = entry.replaceAll(/[+w]/, '')
		int weeks = Integer.valueOf(sanitizedEntry)
		Pair<Date, Date> today = getToday()
		DateTime lowDate = new DateTime(today.getaValue())
		DateTime highDate = new DateTime(today.getbValue())
		if (weeks < 0) {
			lowDate = lowDate.minusWeeks(weeks * -1)
		} else {
			highDate = highDate.plusWeeks(weeks)
		}
		return new Pair(lowDate.toDate(), highDate.toDate())
	}

	private static Pair<Date, Date> getMonthRange(String entry) {
		String sanitizedEntry = entry.replaceAll(/[+M]/, '')
		int months = Integer.valueOf(sanitizedEntry)
		Pair<Date, Date> today = getToday()
		DateTime lowDate = new DateTime(today.getaValue())
		DateTime highDate = new DateTime(today.getbValue())
		if (months < 0) {
			lowDate = lowDate.minusMonths(months * -1)
		} else {
			highDate = highDate.plusMonths(months)
		}
		return new Pair(lowDate.toDate(), highDate.toDate())
	}

}
