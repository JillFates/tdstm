import com.tdssrc.grails.TimeUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import spock.lang.Issue
import test.AbstractUnitSpec

import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat

import static com.tdssrc.grails.TimeUtil.ABBREVIATED
import static com.tdssrc.grails.TimeUtil.FULL

/**
 * Unit test cases for the TimeUtil class
 */
@Mock([UserLogin, UserPreference])
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
		TimeDuration end = new TimeDuration(366,0, 20, 5, 0)
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
		userPrefFormat			| formatName					| formatResult
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE			| "10/05/2017"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE			| "05/10/2017"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME		| "10/05/2017 02:15 PM"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME		| "05/10/2017 02:15 PM"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_2	| "10-05-2017 02:15:30 PM"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_2	| "05-10-2017 02:15:30 PM"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_3	| "Thu, 5 Oct at  02:15 PM"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_3	| "Thu, 5 Oct at  02:15 PM"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_4	| "10/05 14:15"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_4	| "05/10 14:15"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_5	| "20171005"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_5	| "20171005"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_6	| "2017-10-05"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_6	| "2017-10-05"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_7	| "Oct-05"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_7	| "05-Oct"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_8	| "Oct 05,2017 02:15 PM"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_8	| "05 Oct 2017 02:15 PM"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_9	| "10-05-2017 02:15 PM"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_9	| "05-10-2017 02:15 PM"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_10	| "Oct 05"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_10	| "05 Oct"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_11	| "2017/10/05 02:15:30 PM"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_11	| "2017/10/05 02:15:30 PM"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_12	| "10-05-2017"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_12	| "05-10-2017"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_13	| "10/05 14:15:30"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_13	| "05/10 14:15:30"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_14	| "2017-10-05 02:15"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_14	| "2017-10-05 02:15"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_15	| "2017-10-05 14:15:30"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_15	| "2017-10-05 14:15:30"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_16	| "2017-10-05 02:15 PM"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_16	| "2017-10-05 02:15 PM"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_17	| "10/05"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_17	| "05/10"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_18	| "10/5"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_18	| "5/10"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_19	| "10/5 14:15"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_19	| "10/5 14:15"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_20	| "02:15"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_20	| "02:15"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_21	| "15/05"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_21	| "05/15"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_22	| "10/05/2017 02:15:30 PM"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_22	| "05/10/2017 02:15:30 PM"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_23	| "10/05/17"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_23	| "05/10/17"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_24	| "10/05/2017 02:15:30"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_24	| "05/10/2017 02:15:30"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_25	| "10/05/2017 02:15"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_25	| "05/10/2017 02:15"
		TimeUtil.MIDDLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_26	| "20171005_1415"
		TimeUtil.LITTLE_ENDIAN	| TimeUtil.FORMAT_DATE_TIME_26	| "20171005_1415"
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
		long oneDay = 60 * 60 * 24 * 1000
		Timestamp timestamp = new Timestamp(oneDay).clearTime()
		def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)

		expect:
		TimeUtil.formatDate('GMT', timestamp, formatter) == '02/01/1970'
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
		long oneDay = 60 * 60 * 24 * 1000
		Timestamp timestamp = new Timestamp(oneDay).clearTime()
		def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)

		expect:
		TimeUtil.formatDateTime('GMT', timestamp, formatter) == '02/01/1970'
	}

	@Issue('https://support.transitionmanager.com/browse/TM-4823')
	void 'Test formatDateTime(String tzId, Long dateValue, DateFormat formatter)'() {
		// TM-4823
		given:
		// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
		long oneDay = 60 * 60 * 24 * 1000
		Long timeAsLong = new Timestamp(oneDay).clearTime().time
		def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)

		expect:
		TimeUtil.formatDateTime('GMT', timeAsLong, formatter) == '02/01/1970'
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

	void "Test formatTimeDuration(timeDuration, includeSeconds, includeMillis)"(){
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

}
