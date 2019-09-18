import com.tdssrc.grails.TimeUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import net.transitionmanager.person.Person
import net.transitionmanager.security.UserLogin
import net.transitionmanager.person.UserPreference
import spock.lang.Issue
import spock.lang.Unroll
import test.AbstractUnitSpec

import java.sql.Timestamp
import java.text.SimpleDateFormat

import static com.tdssrc.grails.TimeUtil.ABBREVIATED
import static com.tdssrc.grails.TimeUtil.FULL

/**
 * Unit test cases for the TimeUtil class
 */
@Mock([UserLogin, UserPreference, Person])
@TestMixin(ControllerUnitTestMixin)
class TimeUtilTests extends AbstractUnitSpec {

	void setup() {
		login()
	}

	void testAgoWithSeconds() {
		expect:
			'25s' == TimeUtil.ago(25)
			'3m 5s' == TimeUtil.ago(185)
			'1-day 1-hr' == TimeUtil.ago(24 * 60 * 60 + 60 * 60 + 61, ABBREVIATED)
			'2-days 2-hrs' == TimeUtil.ago(24 * 60 * 60 * 2 + 60 * 60 * 2 + 61 * 2, ABBREVIATED)
			'3-days 3-hours' == TimeUtil.ago(24 * 60 * 60 * 3 + 60 * 60 * 3 + 61 * 3, FULL)
			'3-hours 1-minute' == TimeUtil.ago(60 * 60 * 3 + 60, FULL)
			'1-minute 12-seconds' == TimeUtil.ago(72, FULL)
	}

	void testAgoWithTwoDates() {
		given:
			Date start = new Date()
			Date end
			boolean validDates = true
			use(TimeCategory) {
				end = start + 1.day + 2.hours + 5.minutes + 21.seconds

				validDates = validDates && ('1d 2h' == TimeUtil.ago(start, end))
				validDates = validDates && ('1-day 2-hrs' == TimeUtil.ago(start, end, ABBREVIATED))
				validDates = validDates && ('1-day 2-hours' == TimeUtil.ago(start, end, FULL))

				end = start + 3.hours

				validDates = validDates && ('3h' == TimeUtil.ago(start, end))

				end = start + 3.hours + 24.minutes

				validDates = validDates && ('3h 24m' == TimeUtil.ago(start, end))

				end = start + 3.hours + 11.seconds

				validDates = validDates && ('3h' == TimeUtil.ago(start, end))
			}

		expect:
			validDates
	}

	void testAgoWithTimeDuration() {
		given:
			TimeDuration start = new TimeDuration(0, 30, 5, 0)
			TimeDuration end = new TimeDuration(0, 35, 7, 0)
			TimeDuration diff = end - start

		expect:
			'5m 2s' == TimeUtil.ago(diff)
	}

	void testAgoWithInvertedTimeDuration() {
		when:
			TimeDuration est = new TimeDuration(0, 30, 0, 0)
			TimeDuration act = new TimeDuration(0, 20, 0, 0)
			TimeDuration delta = act - est

		then:
			'-10m' == TimeUtil.ago(delta, TimeUtil.SHORT)

		when:
			est = new TimeDuration(0, 30, 35, 0)
			act = new TimeDuration(0, 20, 20, 0)
			delta = act - est

		then:
			'-9m 45s' == TimeUtil.ago(delta)

		when:
			est = new TimeDuration(0, 1, 0, 0)
			act = new TimeDuration(0, 0, 12, 0)
			delta = act - est

		then:
			'-48s' == TimeUtil.ago(delta)
	}

	void testAgoWithYears() {
		given:
			TimeDuration start = new TimeDuration(0, 20, 5, 0)
			TimeDuration end = new TimeDuration(366, 0, 20, 5, 0)
			TimeDuration diff = end - start

		expect:
			'1y 1d' == TimeUtil.ago(diff)
	}

	void testParseDate() {
		given:
			// ******************************************
			// Test dates using date format: "MM/DD/YYYY"

			def testDate = new Date()
			testDate.clearTime()
			testDate.set(year: 2014, month: 9, date: 5)

		expect:
			testDate == TimeUtil.parseDate('10/5/2014')
	}

	void testParseDateTime() {
		given:
			def testDate = new Date(Date.UTC(114, 9, 5, 10, 15, 0))

		expect:
			testDate == TimeUtil.parseDateTime('10/5/2014 10:15 AM')
	}

	void 'Test the createFormatterForType with various options'() {
		expect: """Iterating through the various date/datetime formats that the createFormatterForType method returns
				a valid DateFormat object for both the middle-endian and little-endian date formats """

			def testDate = new Date()

			testDate.clearTime()
			testDate.set(year: 2017, month: 9, date: 5)
			testDate.setHours(14)
			testDate.setMinutes(15)
			testDate.setSeconds(30)


			TimeUtil.createFormatterForType(userPrefFormat, formatName).format(testDate) == formatResult

		where:
			userPrefFormat         | formatName                   | formatResult
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE         | "10/05/2017"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE         | "05/10/2017"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME    | "10/05/2017 02:15 PM"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME    | "05/10/2017 02:15 PM"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_2  | "10-05-2017 02:15:30 PM"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_2  | "05-10-2017 02:15:30 PM"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_3  | "Thu, 5 Oct at  02:15 PM"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_3  | "Thu, 5 Oct at  02:15 PM"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_4  | "10/05 14:15"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_4  | "05/10 14:15"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_5  | "20171005"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_5  | "20171005"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_7  | "Oct-05"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_7  | "05-Oct"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_8  | "Oct 05,2017 02:15 PM"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_8  | "05 Oct 2017 02:15 PM"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_9  | "10-05-2017 02:15 PM"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_9  | "05-10-2017 02:15 PM"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_10 | "Oct 05"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_10 | "05 Oct"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_11 | "2017/10/05 02:15:30 PM"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_11 | "2017/10/05 02:15:30 PM"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_12 | "10-05-2017"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_12 | "05-10-2017"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_13 | "10/05 14:15:30"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_13 | "05/10 14:15:30"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_14 | "2017-10-05 02:15"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_14 | "2017-10-05 02:15"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_15 | "2017-10-05 14:15:30"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_15 | "2017-10-05 14:15:30"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_16 | "2017-10-05 02:15 PM"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_16 | "2017-10-05 02:15 PM"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_17 | "10/05"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_17 | "05/10"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_18 | "10/5"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_18 | "5/10"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_19 | "10/5 14:15"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_19 | "10/5 14:15"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_20 | "02:15"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_20 | "02:15"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_21 | "15/05"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_21 | "05/15"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_22 | "10/05/2017 02:15:30 PM"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_22 | "05/10/2017 02:15:30 PM"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_23 | "10/05/17"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_23 | "05/10/17"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_24 | "10/05/2017 02:15:30"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_24 | "05/10/2017 02:15:30"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_25 | "10/05/2017 02:15"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_25 | "05/10/2017 02:15"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_26 | "20171005_1415"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_TIME_26 | "20171005_1415"
			TimeUtil.MIDDLE_ENDIAN | TimeUtil.FORMAT_DATE_ISO8601 | "2017-10-05"
			TimeUtil.LITTLE_ENDIAN | TimeUtil.FORMAT_DATE_ISO8601 | "2017-10-05"
	}

	//
	// formatDate tests
	//F

	@Issue('https://support.transitionmanager.com/browse/TM-4795')
	void 'Test formatDate(Date dateValue) and formatDate(Date dateValue, DateFormat formatter)'() {
		// No signature of method: static com.tdssrc.grails.TimeUtil.formatDate() is applicable for argument types:
		// (org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession, java.sql.Timestamp)
		// TM-4795
		given:
			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			long oneDay = 60 * 60 * 24 * 1000
			Timestamp ts = new Timestamp(oneDay)
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)

		expect:
			TimeUtil.formatDate(ts) == '01/02/1970'
			TimeUtil.formatDate(ts, formatter) == '02/01/1970'
	}

	void 'Test formatDate(String tzId, Date dateValue, DateFormat formatter)'() {
		given:
			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			Timestamp timestamp = Timestamp.valueOf('1970-01-01 00:00:00')
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)
			def result = TimeUtil.formatDate('GMT', timestamp, formatter)
		expect:
			result == '01/01/1970'
	}

	void 'Test formatDateTime(dateValue, DateFormat formatter)'() {
		when:
			TimeUtil.formatDateTime(new Date(), null)

		then: 'Make sure that a null formatter causes an exception'
			// thrown(InvalidParamException)
			thrown(RuntimeException)
	}

	void 'Test formatDateTime(String tzId, Date dateValue, DateFormat formatter)'() {
		given:
			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			Timestamp timestamp = Timestamp.valueOf('1970-01-01 00:00:00')
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)

		expect:
			TimeUtil.formatDateTime('GMT', timestamp, formatter) == '01/01/1970'
	}

	@Issue('https://support.transitionmanager.com/browse/TM-4823')
	void 'Test formatDateTime(String tzId, Long dateValue, DateFormat formatter)'() {
		// TM-4823
		given:
			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			Long timeAsLong = Timestamp.valueOf('1970-01-01 00:00:00').time
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)

		expect:
			TimeUtil.formatDateTime('GMT', timeAsLong, formatter) == '01/01/1970'
	}

	void 'Test formatDateTimeWithTZ(String tzId, dateValue, DateFormat formatter)'() {
		when:
			TimeUtil.formatDateTime('GMT', new Date(), null)

		then: 'Make sure that a null formatter causes an exception'
			// thrown(InvalidParamException)
			thrown(RuntimeException)
	}

	void 'Test ParseDate when passing blank or null date values'() {
		given:
			def formatter = TimeUtil.createFormatterForType(TimeUtil.MIDDLE_ENDIAN, TimeUtil.FORMAT_DATE)
			def blankValueDate = TimeUtil.parseDate("", formatter)
			def nullValueDate = TimeUtil.parseDate(null, formatter)

		expect:
			blankValueDate == null
			nullValueDate == null
	}

	void "Test sessions without a Time Zone doesn't break formatDateTime"() {
		given:
			session.removeAttribute("CURR_TZ")
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)

		expect:
			TimeUtil.formatDateTime(new Date(), formatter) != null
	}

	void "Test formatDateTimeWithTZ with null date value"() {
		given:
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)

		expect:
			TimeUtil.formatDateTimeWithTZ("GMT", null, formatter) == ''
	}

	void "Test formatTimeDuration(timeDuration, includeSeconds, includeMillis)"() {
		given:
			TimeDuration td1 = new TimeDuration(10, 10, 10, 10, 10)
			TimeDuration td2 = new TimeDuration(100, 10, 10, 10, 10)
			TimeDuration td3 = new TimeDuration(0, 0, 0, 0, 0)
			TimeDuration td4 = new TimeDuration(1, 2, 3, 4, 5)

		expect:
			// Testing with regular values
			TimeUtil.formatTimeDuration(td1) == "10:10:10"

			// Testing with regular values (all accepted fields)
			TimeUtil.formatTimeDuration(td1, true, true) == "10:10:10:10:10"

			// Testing with a bigger than 99 value
			TimeUtil.formatTimeDuration(td2) == "100:10:10"

			// Testing with all zeros
			TimeUtil.formatTimeDuration(td3) == "00:00:00"

			//  Testing with regular values (1-digit values)
			TimeUtil.formatTimeDuration(td4) == "01:02:03"

			// Testing with all the accepted fields (1-digit values).
			TimeUtil.formatTimeDuration(td4, true, true) == "01:02:03:04:05"

			// Testing with seconds and without millis
			TimeUtil.formatTimeDuration(td4, true) == "01:02:03:04"

			// Testing with conflicting params
			TimeUtil.formatTimeDuration(td4, false, true) == "01:02:03"

			// Testing with a null TimeDuration
			TimeUtil.formatTimeDuration(null) == "00:00:00"

			// Testing with a null TimeDuration, including secs and millis.
			TimeUtil.formatTimeDuration(null, true, true) == "00:00:00:00:00"
	}

	void 'Test moveDateToGMT(Date date, String fromTZ) and adjustDateFromGMTToTZ(Date date, String toTZ)'() {

		when: 'we move a java.util.Date from Japan Timezone (9 hours ahead of GMT) to GMT Time'
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyy hh");

			Date originalDate = sdf.parse('06/20/2017 09') // Interpret this as Japan time, (GMT+9) (remember java.util.Date doesn't have Timezone info!)
			Date expectedDate = sdf.parse('06/20/2017 00') // This Date should be returned when converted to GMT

			String japanTimezone = 'Japan'
			Date resultDate = TimeUtil.moveDateToGMT(originalDate, japanTimezone) // Move from Japan Timezone to GMT

		then: 'The resultDate has a 9 hours less than the originalDate'
			resultDate.compareTo(expectedDate) == 0

		when: ' we move a java.util.Date from GMT to Japan Timezone (9 hours ahead of GMT)'
			originalDate = sdf.parse('06/20/2017 00') // Interpret this as GMT (remember java.util.Date doesn't have Timezone info!)
			expectedDate = sdf.parse('06/20/2017 09') // This Date should be returned when converted to GMT Japan time, (GMT+9)

			resultDate = TimeUtil.adjustDateFromGMTToTZ(originalDate, japanTimezone) // Move from GMT to Japan Timezone
		then: 'The resultDate has a 9 hours more than the originalDate'
			resultDate.compareTo(expectedDate) == 0
	}

	void 'Test agoOrIndefinitely'() {
		setup: 'Create a few dates in the near feature and also way ahead.'
			Date now = TimeUtil.nowGMT()
			Date thirtyYearsAhead = now + 11000
			Date nextYear = now + 365
			Date lastYear = now - 365
			Date thirtyYearsAgo = now - 11000
		when: 'Using agoOrIndefinitely with the default values'
			String ago1 = TimeUtil.agoOrIndefinitely(now, nextYear)
			String ago2 = TimeUtil.agoOrIndefinitely(now, thirtyYearsAhead)
		then: 'Neither value is Indefinitely'
			ago1 != 'Indefinitely'
			ago2 != 'Indefinitely'
		when: 'Using agoOrIndefinitely with a threshold set to 10 years'
			ago1 = TimeUtil.agoOrIndefinitely(now, nextYear, 10)
			ago2 = TimeUtil.agoOrIndefinitely(now, thirtyYearsAhead, 10)
		then: 'The date to next year returned an actual value while the one 30 years in the future returned Indefinitely'
			ago1 != 'Indefinitely'
			ago2 == 'Indefinitely'
		when: 'Testing agoOrIndefinitely with a custom label'
			String customLabel = 'XYZ'
			ago1 = TimeUtil.agoOrIndefinitely(now, nextYear, 10, customLabel)
			ago2 = TimeUtil.agoOrIndefinitely(now, thirtyYearsAhead, 10, customLabel)
		then: 'The custom label is returned when appropriately'
			ago1 != customLabel
			ago2 == customLabel
		when: 'Calling with null dates'
			ago1 = TimeUtil.agoOrIndefinitely(null, null)
		then: 'The method returned a null'
			ago1 == null
		when: 'Sending the dates in the wrong order'
			ago1 = TimeUtil.agoOrIndefinitely(nextYear, now, 10, customLabel)
			ago2 = TimeUtil.agoOrIndefinitely(thirtyYearsAhead, now, 10, customLabel)
		then: 'The correct label is still used (negative differences should not matter)'
			ago1 != customLabel
			ago2 == customLabel
	}

	void 'Test hence'() {
		setup: 'Create a couple of dates for testing different scenarios'
			Date now = TimeUtil.nowGMT()
			Date thirtyYearsAhead = now + 11000
			Date nextYear = now + 365
			Date lastYear = now - 365
			Date thirtyYearsAgo = now - 11000
		when: 'Testing without threshold parameters'
			String hence1 = TimeUtil.hence(nextYear)
		then: 'The value is not the default label'
			hence1 != 'Indefinitely'
		when: 'Testing with threshold'
			hence1 = TimeUtil.hence(nextYear, 10)
			String hence2 = TimeUtil.hence(thirtyYearsAhead, 10)
		then: 'The default label is used only in the second case'
			hence1 != 'Indefinitely'
			hence2 == 'Indefinitely'
		when: 'Using a threshold and a custom label'
			String label = 'Way in the future'
			hence1 = TimeUtil.hence(nextYear, 10, label)
			hence2 = TimeUtil.hence(thirtyYearsAhead, 10, label)
		then: 'The custom label was used only for the second case'
			hence1 != label
			hence1 != 'Indefinitely'
			hence2 == label
		when: 'Testing with a null date'
			hence1 = TimeUtil.hence(null)
		then: 'The result is null'
			hence1 == null
		when: 'Testing with a date in the past and a threshold'
			hence1 = TimeUtil.hence(lastYear, 10, label)
			hence2 = TimeUtil.hence(thirtyYearsAgo, 10, label)
		then: 'Only the second case returned the label'
			hence1 != label
			hence2 == label
	}

	void 'Test the canParseDate function with various date strings'() {
		expect: "Using various date strings, validates if it's a parsable ISO8601 date returns the correct result"

			TimeUtil.canParseDate(dateString) == validationResult

		where:
			dateString   | validationResult
			'1979-08-02' | true
			'1979/08/02' | false
	}

	void 'Test the canParseDateTime function with various date strings'() {
		expect: "Using various datetime strings, validates if it's a parsable ISO8601 date time returns the correct result"

			TimeUtil.canParseDateTime(dateTimeString) == validationResult

		where:
			dateTimeString         | validationResult
			"1979-08-02T10:00:00Z" | true
			"1979-08-02T10:00Z"    | true
			"1979/08/02T10:00:00Z" | false
	}

	@Unroll
	void 'Test #timeZone has #timeZoneOffset offset'(){
		expect:'getTimeZoneOffset for a given time zone will qeual the offset'
			TimeUtil.getTimeZoneOffset(timeZone) == timeZoneOffset
		where:
			timeZone                           | timeZoneOffset
			TimeUtil.defaultTimeZone           | TimeUtil.GMT_OFFSET
			'Etc/GMT+12'                       | '-12:00'
			'Pacific/Pago_Pago'                | '-11:00'
			'Pacific/Samoa'                    | '-11:00'
			'Pacific/Niue'                     | '-11:00'
			'US/Samoa'                         | '-11:00'
			'Etc/GMT+11'                       | '-11:00'
			'Pacific/Midway'                   | '-11:00'
			'Pacific/Honolulu'                 | '-10:00'
			'Pacific/Rarotonga'                | '-10:00'
			'Pacific/Tahiti'                   | '-10:00'
			'Pacific/Johnston'                 | '-10:00'
			'US/Hawaii'                        | '-10:00'
			'SystemV/HST10'                    | '-10:00'
			'Etc/GMT+10'                       | '-10:00'
			'Pacific/Marquesas'                | '-09:30'
			'Etc/GMT+9'                        | '-09:00'
			'Pacific/Gambier'                  | '-09:00'
			'America/Atka'                     | '-09:00'
			'SystemV/YST9'                     | '-09:00'
			'America/Adak'                     | '-09:00'
			'US/Aleutian'                      | '-09:00'
			'Etc/GMT+8'                        | '-08:00'
			'US/Alaska'                        | '-08:00'
			'America/Juneau'                   | '-08:00'
			'America/Metlakatla'               | '-08:00'
			'America/Yakutat'                  | '-08:00'
			'Pacific/Pitcairn'                 | '-08:00'
			'America/Sitka'                    | '-08:00'
			'America/Anchorage'                | '-08:00'
			'SystemV/PST8'                     | '-08:00'
			'America/Nome'                     | '-08:00'
			'SystemV/YST9YDT'                  | '-08:00'
			'Canada/Yukon'                     | '-07:00'
			'US/Pacific-New'                   | '-07:00'
			'Etc/GMT+7'                        | '-07:00'
			'US/Arizona'                       | '-07:00'
			'America/Dawson_Creek'             | '-07:00'
			'Canada/Pacific'                   | '-07:00'
			'PST8PDT'                          | '-07:00'
			'SystemV/MST7'                     | '-07:00'
			'America/Dawson'                   | '-07:00'
			'Mexico/BajaNorte'                 | '-07:00'
			'America/Tijuana'                  | '-07:00'
			'America/Creston'                  | '-07:00'
			'America/Hermosillo'               | '-07:00'
			'America/Santa_Isabel'             | '-07:00'
			'America/Vancouver'                | '-07:00'
			'America/Ensenada'                 | '-07:00'
			'America/Phoenix'                  | '-07:00'
			'America/Whitehorse'               | '-07:00'
			'America/Fort_Nelson'              | '-07:00'
			'SystemV/PST8PDT'                  | '-07:00'
			'America/Los_Angeles'              | '-07:00'
			'US/Pacific'                       | '-07:00'
			'America/El_Salvador'              | '-06:00'
			'America/Guatemala'                | '-06:00'
			'America/Belize'                   | '-06:00'
			'America/Managua'                  | '-06:00'
			'America/Tegucigalpa'              | '-06:00'
			'Etc/GMT+6'                        | '-06:00'
			'Mexico/BajaSur'                   | '-06:00'
			'America/Regina'                   | '-06:00'
			'America/Denver'                   | '-06:00'
			'Pacific/Galapagos'                | '-06:00'
			'America/Yellowknife'              | '-06:00'
			'America/Swift_Current'            | '-06:00'
			'America/Inuvik'                   | '-06:00'
			'America/Mazatlan'                 | '-06:00'
			'America/Boise'                    | '-06:00'
			'America/Costa_Rica'               | '-06:00'
			'MST7MDT'                          | '-06:00'
			'SystemV/CST6'                     | '-06:00'
			'America/Chihuahua'                | '-06:00'
			'America/Ojinaga'                  | '-06:00'
			'US/Mountain'                      | '-06:00'
			'America/Edmonton'                 | '-06:00'
			'Canada/Mountain'                  | '-06:00'
			'America/Cambridge_Bay'            | '-06:00'
			'Navajo'                           | '-06:00'
			'SystemV/MST7MDT'                  | '-06:00'
			'Canada/Saskatchewan'              | '-06:00'
			'America/Shiprock'                 | '-06:00'
			'America/Panama'                   | '-05:00'
			'America/Chicago'                  | '-05:00'
			'America/Eirunepe'                 | '-05:00'
			'Etc/GMT+5'                        | '-05:00'
			'Mexico/General'                   | '-05:00'
			'America/Porto_Acre'               | '-05:00'
			'America/Guayaquil'                | '-05:00'
			'America/Rankin_Inlet'             | '-05:00'
			'US/Central'                       | '-05:00'
			'America/Rainy_River'              | '-05:00'
			'America/Indiana/Knox'             | '-05:00'
			'America/North_Dakota/Beulah'      | '-05:00'
			'America/Monterrey'                | '-05:00'
			'America/Jamaica'                  | '-05:00'
			'America/Atikokan'                 | '-05:00'
			'America/Coral_Harbour'            | '-05:00'
			'America/North_Dakota/Center'      | '-05:00'
			'America/Cayman'                   | '-05:00'
			'America/Indiana/Tell_City'        | '-05:00'
			'America/Mexico_City'              | '-05:00'
			'America/Matamoros'                | '-05:00'
			'CST6CDT'                          | '-05:00'
			'America/Knox_IN'                  | '-05:00'
			'America/Bogota'                   | '-05:00'
			'America/Menominee'                | '-05:00'
			'America/Resolute'                 | '-05:00'
			'SystemV/EST5'                     | '-05:00'
			'Canada/Central'                   | '-05:00'
			'Brazil/Acre'                      | '-05:00'
			'America/Cancun'                   | '-05:00'
			'America/Lima'                     | '-05:00'
			'America/Bahia_Banderas'           | '-05:00'
			'US/Indiana-Starke'                | '-05:00'
			'America/Rio_Branco'               | '-05:00'
			'SystemV/CST6CDT'                  | '-05:00'
			'Jamaica'                          | '-05:00'
			'America/Merida'                   | '-05:00'
			'America/North_Dakota/New_Salem'   | '-05:00'
			'America/Winnipeg'                 | '-05:00'
			'America/Cuiaba'                   | '-04:00'
			'America/Marigot'                  | '-04:00'
			'America/Indiana/Petersburg'       | '-04:00'
			'America/Grand_Turk'               | '-04:00'
			'Cuba'                             | '-04:00'
			'Etc/GMT+4'                        | '-04:00'
			'America/Manaus'                   | '-04:00'
			'America/Fort_Wayne'               | '-04:00'
			'America/St_Thomas'                | '-04:00'
			'America/Anguilla'                 | '-04:00'
			'America/Havana'                   | '-04:00'
			'US/Michigan'                      | '-04:00'
			'America/Barbados'                 | '-04:00'
			'America/Louisville'               | '-04:00'
			'America/Curacao'                  | '-04:00'
			'America/Guyana'                   | '-04:00'
			'America/Martinique'               | '-04:00'
			'America/Puerto_Rico'              | '-04:00'
			'America/Port_of_Spain'            | '-04:00'
			'SystemV/AST4'                     | '-04:00'
			'America/Indiana/Vevay'            | '-04:00'
			'America/Indiana/Vincennes'        | '-04:00'
			'America/Kralendijk'               | '-04:00'
			'America/Antigua'                  | '-04:00'
			'America/Indianapolis'             | '-04:00'
			'America/Iqaluit'                  | '-04:00'
			'America/St_Vincent'               | '-04:00'
			'America/Kentucky/Louisville'      | '-04:00'
			'America/Dominica'                 | '-04:00'
			'America/Asuncion'                 | '-04:00'
			'EST5EDT'                          | '-04:00'
			'America/Nassau'                   | '-04:00'
			'America/Kentucky/Monticello'      | '-04:00'
			'Brazil/West'                      | '-04:00'
			'America/Aruba'                    | '-04:00'
			'America/Indiana/Indianapolis'     | '-04:00'
			'America/La_Paz'                   | '-04:00'
			'America/Thunder_Bay'              | '-04:00'
			'America/Indiana/Marengo'          | '-04:00'
			'America/Blanc-Sablon'             | '-04:00'
			'America/Santo_Domingo'            | '-04:00'
			'US/Eastern'                       | '-04:00'
			'Canada/Eastern'                   | '-04:00'
			'America/Port-au-Prince'           | '-04:00'
			'America/St_Barthelemy'            | '-04:00'
			'America/Nipigon'                  | '-04:00'
			'US/East-Indiana'                  | '-04:00'
			'America/St_Lucia'                 | '-04:00'
			'America/Montserrat'               | '-04:00'
			'America/Lower_Princes'            | '-04:00'
			'America/Detroit'                  | '-04:00'
			'America/Tortola'                  | '-04:00'
			'America/Porto_Velho'              | '-04:00'
			'America/Campo_Grande'             | '-04:00'
			'America/Virgin'                   | '-04:00'
			'America/Pangnirtung'              | '-04:00'
			'America/Montreal'                 | '-04:00'
			'America/Indiana/Winamac'          | '-04:00'
			'America/Boa_Vista'                | '-04:00'
			'America/Grenada'                  | '-04:00'
			'America/New_York'                 | '-04:00'
			'America/St_Kitts'                 | '-04:00'
			'America/Caracas'                  | '-04:00'
			'America/Guadeloupe'               | '-04:00'
			'America/Toronto'                  | '-04:00'
			'SystemV/EST5EDT'                  | '-04:00'
			'America/Argentina/Catamarca'      | '-03:00'
			'Canada/Atlantic'                  | '-03:00'
			'America/Argentina/Cordoba'        | '-03:00'
			'America/Araguaina'                | '-03:00'
			'America/Argentina/Salta'          | '-03:00'
			'Etc/GMT+3'                        | '-03:00'
			'America/Montevideo'               | '-03:00'
			'Brazil/East'                      | '-03:00'
			'America/Argentina/Mendoza'        | '-03:00'
			'America/Argentina/Rio_Gallegos'   | '-03:00'
			'America/Catamarca'                | '-03:00'
			'America/Cordoba'                  | '-03:00'
			'America/Sao_Paulo'                | '-03:00'
			'America/Argentina/Jujuy'          | '-03:00'
			'America/Cayenne'                  | '-03:00'
			'America/Recife'                   | '-03:00'
			'America/Buenos_Aires'             | '-03:00'
			'America/Paramaribo'               | '-03:00'
			'America/Moncton'                  | '-03:00'
			'America/Mendoza'                  | '-03:00'
			'America/Santarem'                 | '-03:00'
			'Atlantic/Bermuda'                 | '-03:00'
			'America/Maceio'                   | '-03:00'
			'Atlantic/Stanley'                 | '-03:00'
			'America/Halifax'                  | '-03:00'
			'Antarctica/Rothera'               | '-03:00'
			'America/Argentina/San_Luis'       | '-03:00'
			'America/Argentina/Ushuaia'        | '-03:00'
			'Antarctica/Palmer'                | '-03:00'
			'America/Punta_Arenas'             | '-03:00'
			'America/Glace_Bay'                | '-03:00'
			'America/Fortaleza'                | '-03:00'
			'America/Thule'                    | '-03:00'
			'America/Argentina/La_Rioja'       | '-03:00'
			'America/Belem'                    | '-03:00'
			'America/Jujuy'                    | '-03:00'
			'America/Bahia'                    | '-03:00'
			'America/Goose_Bay'                | '-03:00'
			'America/Argentina/San_Juan'       | '-03:00'
			'America/Argentina/ComodRivadavia' | '-03:00'
			'America/Argentina/Tucuman'        | '-03:00'
			'America/Rosario'                  | '-03:00'
			'SystemV/AST4ADT'                  | '-03:00'
			'America/Argentina/Buenos_Aires'   | '-03:00'
			'America/St_Johns'                 | '-02:30'
			'Canada/Newfoundland'              | '-02:30'
			'America/Miquelon'                 | '-02:00'
			'Etc/GMT+2'                        | '-02:00'
			'America/Godthab'                  | '-02:00'
			'America/Noronha'                  | '-02:00'
			'Brazil/DeNoronha'                 | '-02:00'
			'Atlantic/South_Georgia'           | '-02:00'
			'Etc/GMT+1'                        | '-01:00'
			'Atlantic/Cape_Verde'              | '-01:00'
			'Pacific/Kiritimati'               | '+14:00'
			'Etc/GMT-14'                       | '+14:00'
			'Pacific/Fakaofo'                  | '+13:00'
			'Pacific/Enderbury'                | '+13:00'
			'Pacific/Apia'                     | '+13:00'
			'Pacific/Tongatapu'                | '+13:00'
			'Etc/GMT-13'                       | '+13:00'
			'NZ-CHAT'                          | '+12:45'
			'Pacific/Chatham'                  | '+12:45'
			'Pacific/Kwajalein'                | '+12:00'
			'Antarctica/McMurdo'               | '+12:00'
			'Pacific/Wallis'                   | '+12:00'
			'Pacific/Fiji'                     | '+12:00'
			'Pacific/Funafuti'                 | '+12:00'
			'Pacific/Nauru'                    | '+12:00'
			'Kwajalein'                        | '+12:00'
			'NZ'                               | '+12:00'
			'Pacific/Wake'                     | '+12:00'
			'Antarctica/South_Pole'            | '+12:00'
			'Pacific/Tarawa'                   | '+12:00'
			'Pacific/Auckland'                 | '+12:00'
			'Asia/Kamchatka'                   | '+12:00'
			'Etc/GMT-12'                       | '+12:00'
			'Asia/Anadyr'                      | '+12:00'
			'Pacific/Majuro'                   | '+12:00'
			'Pacific/Ponape'                   | '+11:00'
			'Pacific/Bougainville'             | '+11:00'
			'Antarctica/Macquarie'             | '+11:00'
			'Pacific/Pohnpei'                  | '+11:00'
			'Pacific/Efate'                    | '+11:00'
			'Pacific/Norfolk'                  | '+11:00'
			'Asia/Magadan'                     | '+11:00'
			'Pacific/Kosrae'                   | '+11:00'
			'Asia/Sakhalin'                    | '+11:00'
			'Pacific/Noumea'                   | '+11:00'
			'Etc/GMT-11'                       | '+11:00'
			'Asia/Srednekolymsk'               | '+11:00'
			'Pacific/Guadalcanal'              | '+11:00'
			'Australia/Lord_Howe'              | '+10:30'
			'Australia/LHI'                    | '+10:30'
			'Australia/Hobart'                 | '+10:00'
			'Pacific/Yap'                      | '+10:00'
			'Australia/Tasmania'               | '+10:00'
			'Pacific/Port_Moresby'             | '+10:00'
			'Australia/ACT'                    | '+10:00'
			'Australia/Victoria'               | '+10:00'
			'Pacific/Chuuk'                    | '+10:00'
			'Australia/Queensland'             | '+10:00'
			'Australia/Canberra'               | '+10:00'
			'Australia/Currie'                 | '+10:00'
			'Pacific/Guam'                     | '+10:00'
			'Pacific/Truk'                     | '+10:00'
			'Australia/NSW'                    | '+10:00'
			'Asia/Vladivostok'                 | '+10:00'
			'Pacific/Saipan'                   | '+10:00'
			'Antarctica/DumontDUrville'        | '+10:00'
			'Australia/Sydney'                 | '+10:00'
			'Australia/Brisbane'               | '+10:00'
			'Etc/GMT-10'                       | '+10:00'
			'Asia/Ust-Nera'                    | '+10:00'
			'Australia/Melbourne'              | '+10:00'
			'Australia/Lindeman'               | '+10:00'
			'Australia/North'                  | '+09:30'
			'Australia/Yancowinna'             | '+09:30'
			'Australia/Adelaide'               | '+09:30'
			'Australia/Broken_Hill'            | '+09:30'
			'Australia/South'                  | '+09:30'
			'Australia/Darwin'                 | '+09:30'
			'Etc/GMT-9'                        | '+09:00'
			'Pacific/Palau'                    | '+09:00'
			'Asia/Chita'                       | '+09:00'
			'Asia/Dili'                        | '+09:00'
			'Asia/Jayapura'                    | '+09:00'
			'Asia/Yakutsk'                     | '+09:00'
			'ROK'                              | '+09:00'
			'Asia/Seoul'                       | '+09:00'
			'Asia/Khandyga'                    | '+09:00'
			'Japan'                            | '+09:00'
			'Asia/Tokyo'                       | '+09:00'
			'Australia/Eucla'                  | '+08:45'
			'Asia/Kuching'                     | '+08:00'
			'Asia/Chungking'                   | '+08:00'
			'Etc/GMT-8'                        | '+08:00'
			'Australia/Perth'                  | '+08:00'
			'Asia/Macao'                       | '+08:00'
			'Asia/Macau'                       | '+08:00'
			'Asia/Choibalsan'                  | '+08:00'
			'Asia/Shanghai'                    | '+08:00'
			'Asia/Ulan_Bator'                  | '+08:00'
			'Asia/Chongqing'                   | '+08:00'
			'Asia/Ulaanbaatar'                 | '+08:00'
			'Asia/Taipei'                      | '+08:00'
			'Asia/Manila'                      | '+08:00'
			'PRC'                              | '+08:00'
			'Asia/Ujung_Pandang'               | '+08:00'
			'Asia/Harbin'                      | '+08:00'
			'Singapore'                        | '+08:00'
			'Asia/Brunei'                      | '+08:00'
			'Australia/West'                   | '+08:00'
			'Asia/Hong_Kong'                   | '+08:00'
			'Asia/Makassar'                    | '+08:00'
			'Hongkong'                         | '+08:00'
			'Asia/Kuala_Lumpur'                | '+08:00'
			'Asia/Irkutsk'                     | '+08:00'
			'Asia/Singapore'                   | '+08:00'
			'Asia/Pontianak'                   | '+07:00'
			'Etc/GMT-7'                        | '+07:00'
			'Asia/Phnom_Penh'                  | '+07:00'
			'Asia/Novosibirsk'                 | '+07:00'
			'Antarctica/Davis'                 | '+07:00'
			'Asia/Tomsk'                       | '+07:00'
			'Asia/Jakarta'                     | '+07:00'
			'Asia/Barnaul'                     | '+07:00'
			'Indian/Christmas'                 | '+07:00'
			'Asia/Ho_Chi_Minh'                 | '+07:00'
			'Asia/Hovd'                        | '+07:00'
			'Asia/Bangkok'                     | '+07:00'
			'Asia/Vientiane'                   | '+07:00'
			'Asia/Novokuznetsk'                | '+07:00'
			'Asia/Krasnoyarsk'                 | '+07:00'
			'Asia/Saigon'                      | '+07:00'
			'Asia/Yangon'                      | '+06:30'
			'Asia/Rangoon'                     | '+06:30'
			'Indian/Cocos'                     | '+06:30'
			'Asia/Kashgar'                     | '+06:00'
			'Etc/GMT-6'                        | '+06:00'
			'Asia/Almaty'                      | '+06:00'
			'Asia/Dacca'                       | '+06:00'
			'Asia/Omsk'                        | '+06:00'
			'Asia/Dhaka'                       | '+06:00'
			'Indian/Chagos'                    | '+06:00'
			'Asia/Qyzylorda'                   | '+06:00'
			'Asia/Bishkek'                     | '+06:00'
			'Antarctica/Vostok'                | '+06:00'
			'Asia/Urumqi'                      | '+06:00'
			'Asia/Thimbu'                      | '+06:00'
			'Asia/Thimphu'                     | '+06:00'
			'Asia/Kathmandu'                   | '+05:45'
			'Asia/Katmandu'                    | '+05:45'
			'Asia/Kolkata'                     | '+05:30'
			'Asia/Colombo'                     | '+05:30'
			'Asia/Calcutta'                    | '+05:30'
			'Asia/Aqtau'                       | '+05:00'
			'Etc/GMT-5'                        | '+05:00'
			'Asia/Samarkand'                   | '+05:00'
			'Asia/Karachi'                     | '+05:00'
			'Asia/Yekaterinburg'               | '+05:00'
			'Asia/Dushanbe'                    | '+05:00'
			'Indian/Maldives'                  | '+05:00'
			'Asia/Oral'                        | '+05:00'
			'Asia/Tashkent'                    | '+05:00'
			'Antarctica/Mawson'                | '+05:00'
			'Asia/Aqtobe'                      | '+05:00'
			'Asia/Ashkhabad'                   | '+05:00'
			'Asia/Ashgabat'                    | '+05:00'
			'Asia/Atyrau'                      | '+05:00'
			'Indian/Kerguelen'                 | '+05:00'
			'Iran'                             | '+04:30'
			'Asia/Tehran'                      | '+04:30'
			'Asia/Kabul'                       | '+04:30'
			'Asia/Yerevan'                     | '+04:00'
			'Etc/GMT-4'                        | '+04:00'
			'Asia/Dubai'                       | '+04:00'
			'Indian/Reunion'                   | '+04:00'
			'Indian/Mauritius'                 | '+04:00'
			'Europe/Saratov'                   | '+04:00'
			'Europe/Samara'                    | '+04:00'
			'Indian/Mahe'                      | '+04:00'
			'Asia/Baku'                        | '+04:00'
			'Asia/Muscat'                      | '+04:00'
			'Europe/Astrakhan'                 | '+04:00'
			'Asia/Tbilisi'                     | '+04:00'
			'Europe/Ulyanovsk'                 | '+04:00'
			'Asia/Aden'                        | '+03:00'
			'Africa/Nairobi'                   | '+03:00'
			'Europe/Istanbul'                  | '+03:00'
			'Etc/GMT-3'                        | '+03:00'
			'Europe/Zaporozhye'                | '+03:00'
			'Israel'                           | '+03:00'
			'Indian/Comoro'                    | '+03:00'
			'Antarctica/Syowa'                 | '+03:00'
			'Africa/Mogadishu'                 | '+03:00'
			'Europe/Bucharest'                 | '+03:00'
			'Africa/Asmera'                    | '+03:00'
			'Europe/Mariehamn'                 | '+03:00'
			'Asia/Istanbul'                    | '+03:00'
			'Europe/Tiraspol'                  | '+03:00'
			'Europe/Moscow'                    | '+03:00'
			'Europe/Chisinau'                  | '+03:00'
			'Europe/Helsinki'                  | '+03:00'
			'Asia/Beirut'                      | '+03:00'
			'Asia/Tel_Aviv'                    | '+03:00'
			'Africa/Djibouti'                  | '+03:00'
			'Europe/Simferopol'                | '+03:00'
			'Europe/Sofia'                     | '+03:00'
			'Asia/Gaza'                        | '+03:00'
			'Africa/Asmara'                    | '+03:00'
			'Europe/Riga'                      | '+03:00'
			'Asia/Baghdad'                     | '+03:00'
			'Asia/Damascus'                    | '+03:00'
			'Africa/Dar_es_Salaam'             | '+03:00'
			'Africa/Addis_Ababa'               | '+03:00'
			'Europe/Uzhgorod'                  | '+03:00'
			'Asia/Jerusalem'                   | '+03:00'
			'Asia/Riyadh'                      | '+03:00'
			'Asia/Kuwait'                      | '+03:00'
			'Europe/Kirov'                     | '+03:00'
			'Africa/Kampala'                   | '+03:00'
			'Europe/Minsk'                     | '+03:00'
			'Asia/Qatar'                       | '+03:00'
			'Europe/Kiev'                      | '+03:00'
			'Asia/Bahrain'                     | '+03:00'
			'Europe/Vilnius'                   | '+03:00'
			'Indian/Antananarivo'              | '+03:00'
			'Indian/Mayotte'                   | '+03:00'
			'Europe/Tallinn'                   | '+03:00'
			'Turkey'                           | '+03:00'
			'Africa/Juba'                      | '+03:00'
			'Asia/Nicosia'                     | '+03:00'
			'Asia/Famagusta'                   | '+03:00'
			'W-SU'                             | '+03:00'
			'EET'                              | '+03:00'
			'Asia/Hebron'                      | '+03:00'
			'Asia/Amman'                       | '+03:00'
			'Europe/Nicosia'                   | '+03:00'
			'Europe/Athens'                    | '+03:00'
			'Africa/Cairo'                     | '+02:00'
			'Africa/Mbabane'                   | '+02:00'
			'Europe/Brussels'                  | '+02:00'
			'Europe/Warsaw'                    | '+02:00'
			'CET'                              | '+02:00'
			'Europe/Luxembourg'                | '+02:00'
			'Etc/GMT-2'                        | '+02:00'
			'Libya'                            | '+02:00'
			'Africa/Kigali'                    | '+02:00'
			'Africa/Tripoli'                   | '+02:00'
			'Europe/Kaliningrad'               | '+02:00'
			'Africa/Windhoek'                  | '+02:00'
			'Europe/Malta'                     | '+02:00'
			'Europe/Busingen'                  | '+02:00'
			'Europe/Skopje'                    | '+02:00'
			'Europe/Sarajevo'                  | '+02:00'
			'Europe/Rome'                      | '+02:00'
			'Europe/Zurich'                    | '+02:00'
			'Europe/Gibraltar'                 | '+02:00'
			'Africa/Lubumbashi'                | '+02:00'
			'Europe/Vaduz'                     | '+02:00'
			'Europe/Ljubljana'                 | '+02:00'
			'Europe/Berlin'                    | '+02:00'
			'Europe/Stockholm'                 | '+02:00'
			'Europe/Budapest'                  | '+02:00'
			'Europe/Zagreb'                    | '+02:00'
			'Europe/Paris'                     | '+02:00'
			'Africa/Ceuta'                     | '+02:00'
			'Europe/Prague'                    | '+02:00'
			'Antarctica/Troll'                 | '+02:00'
			'Africa/Gaborone'                  | '+02:00'
			'Europe/Copenhagen'                | '+02:00'
			'Europe/Vienna'                    | '+02:00'
			'Europe/Tirane'                    | '+02:00'
			'MET'                              | '+02:00'
			'Europe/Amsterdam'                 | '+02:00'
			'Africa/Maputo'                    | '+02:00'
			'Europe/San_Marino'                | '+02:00'
			'Poland'                           | '+02:00'
			'Europe/Andorra'                   | '+02:00'
			'Europe/Oslo'                      | '+02:00'
			'Europe/Podgorica'                 | '+02:00'
			'Africa/Bujumbura'                 | '+02:00'
			'Atlantic/Jan_Mayen'               | '+02:00'
			'Africa/Maseru'                    | '+02:00'
			'Europe/Madrid'                    | '+02:00'
			'Africa/Blantyre'                  | '+02:00'
			'Africa/Lusaka'                    | '+02:00'
			'Africa/Harare'                    | '+02:00'
			'Africa/Khartoum'                  | '+02:00'
			'Africa/Johannesburg'              | '+02:00'
			'Europe/Belgrade'                  | '+02:00'
			'Europe/Bratislava'                | '+02:00'
			'Arctic/Longyearbyen'              | '+02:00'
			'Egypt'                            | '+02:00'
			'Europe/Vatican'                   | '+02:00'
			'Europe/Monaco'                    | '+02:00'
			'Europe/London'                    | '+01:00'
			'Etc/GMT-1'                        | '+01:00'
			'Europe/Jersey'                    | '+01:00'
			'Europe/Guernsey'                  | '+01:00'
			'Europe/Isle_of_Man'               | '+01:00'
			'Africa/Tunis'                     | '+01:00'
			'Africa/Malabo'                    | '+01:00'
			'GB-Eire'                          | '+01:00'
			'Africa/Lagos'                     | '+01:00'
			'Africa/Algiers'                   | '+01:00'
			'GB'                               | '+01:00'
			'Portugal'                         | '+01:00'
			'Africa/Sao_Tome'                  | '+01:00'
			'Africa/Ndjamena'                  | '+01:00'
			'Atlantic/Faeroe'                  | '+01:00'
			'Eire'                             | '+01:00'
			'Atlantic/Faroe'                   | '+01:00'
			'Europe/Dublin'                    | '+01:00'
			'Africa/Libreville'                | '+01:00'
			'Africa/El_Aaiun'                  | '+01:00'
			'Africa/Douala'                    | '+01:00'
			'Africa/Brazzaville'               | '+01:00'
			'Africa/Porto-Novo'                | '+01:00'
			'Atlantic/Madeira'                 | '+01:00'
			'Europe/Lisbon'                    | '+01:00'
			'Atlantic/Canary'                  | '+01:00'
			'Africa/Casablanca'                | '+01:00'
			'Europe/Belfast'                   | '+01:00'
			'Africa/Luanda'                    | '+01:00'
			'Africa/Kinshasa'                  | '+01:00'
			'Africa/Bangui'                    | '+01:00'
			'WET'                              | '+01:00'
			'Africa/Niamey'                    | '+01:00'
			'GMT'                              | '+00:00'
			'Etc/GMT-0'                        | '+00:00'
			'Atlantic/St_Helena'               | '+00:00'
			'Etc/GMT+0'                        | '+00:00'
			'Africa/Banjul'                    | '+00:00'
			'Etc/GMT'                          | '+00:00'
			'Africa/Freetown'                  | '+00:00'
			'Africa/Bamako'                    | '+00:00'
			'Africa/Conakry'                   | '+00:00'
			'Universal'                        | '+00:00'
			'Africa/Nouakchott'                | '+00:00'
			'UTC'                              | '+00:00'
			'Etc/Universal'                    | '+00:00'
			'Atlantic/Azores'                  | '+00:00'
			'Africa/Abidjan'                   | '+00:00'
			'Africa/Accra'                     | '+00:00'
			'Etc/UCT'                          | '+00:00'
			'GMT0'                             | '+00:00'
			'Zulu'                             | '+00:00'
			'Africa/Ouagadougou'               | '+00:00'
			'Atlantic/Reykjavik'               | '+00:00'
			'Etc/Zulu'                         | '+00:00'
			'Iceland'                          | '+00:00'
			'Africa/Lome'                      | '+00:00'
			'Greenwich'                        | '+00:00'
			'Etc/GMT0'                         | '+00:00'
			'America/Danmarkshavn'             | '+00:00'
			'Africa/Dakar'                     | '+00:00'
			'America/Scoresbysund'             | '+00:00'
			'Africa/Bissau'                    | '+00:00'
			'Etc/Greenwich'                    | '+00:00'
			'Africa/Timbuktu'                  | '+00:00'
			'UCT'                              | '+00:00'
			'Africa/Monrovia'                  | '+00:00'
			'Etc/UTC'                          | '+00:00'
	}
}
