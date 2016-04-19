import groovy.time.TimeCategory
import groovy.time.TimeDuration

import com.tdssrc.grails.TimeUtil
import java.text.DateFormat
import java.sql.Timestamp

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the TimeUtil class
 * Note that in order to test with the HttpSession that this test spec is using the AdminController not for any thing in particular
 * but it allows the tests to access the session and manipulate it appropriately.
*/
@TestFor(AdminController)
class TimeUtilTests extends Specification {


	public void testAgoWithSeconds() {
		expect:
		'25s' == TimeUtil.ago(25)
		'3m 5s' == TimeUtil.ago(185)		
		'1-day 1-hr' == TimeUtil.ago( 24*60*60 + 60*60 + 61, TimeUtil.ABBREVIATED )		
		'2-days 2-hrs' == TimeUtil.ago( 24*60*60*2 + 60*60*2 + 61*2, TimeUtil.ABBREVIATED )
		'3-days 3-hours' == TimeUtil.ago( 24*60*60*3 + 60*60*3 + 61*3, TimeUtil.FULL )
		'3-hours 1-minute' == TimeUtil.ago( 60*60*3 + 60, TimeUtil.FULL )
		'1-minute 12-seconds' == TimeUtil.ago( 72, TimeUtil.FULL )
	}

	public void testAgoWithTwoDates() {
		Date start = new Date()
		Date end
		def validDates = true
		use( TimeCategory ) {
			end = start + 1.day + 2.hours + 5.minutes + 21.seconds

			validDates = validDates && ('1d 2h' == TimeUtil.ago(start, end))
			validDates = validDates && ('1-day 2-hrs' == TimeUtil.ago(start, end, TimeUtil.ABBREVIATED))
			validDates = validDates && ('1-day 2-hours' == TimeUtil.ago(start, end, TimeUtil.FULL))

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

	public void testAgoWithTimeDuration() {
		TimeDuration start = new TimeDuration(0, 30, 5, 0)
		TimeDuration end = new TimeDuration(0, 35, 7, 0)
		TimeDuration diff = end - start
		expect:
			'5m 2s' == TimeUtil.ago(diff)
	}

	public void testAgoWithInvertedTimeDuration() {

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

	public void testParseDate() {
		// ******************************************
		// Test dates using date format: "MM/DD/YYYY"
		def sessionDateFormat = "MM/DD/YYYY"
		def sessionTimeZone = "GMT"

		// Mock session behaviour
		def session = [
			"getAttribute": { param ->
				if ("CURR_DT_FORMAT" == param) {
					return ["CURR_DT_FORMAT": sessionDateFormat]
				} else if ("CURR_TZ" == param) {
					return ["CURR_TZ": sessionTimeZone]
				}
			}
		]

		def testDate = new Date()
		testDate.clearTime()
		testDate.set(year: 2014, month: 9, date: 5)

		expect:
			testDate.equals(TimeUtil.parseDate(session, '10/5/2014'))
	}

	public void testParseDateTime() {
		def sessionDateFormat = "MM/DD/YYYY"
		def sessionTimeZone = "GMT"

		// Mock session behaviour
		def session = [
			"getAttribute": { param ->
				if ("CURR_DT_FORMAT" == param) {
					return ["CURR_DT_FORMAT": sessionDateFormat]
				} else if ("CURR_TZ" == param) {
					return ["CURR_TZ": sessionTimeZone]
				}
			}
		]

		def testDate = new Date(Date.UTC(114, 9, 5, 10, 15, 0))

		expect:
			testDate.equals(TimeUtil.parseDateTime(session, '10/5/2014 10:15 AM'))
	}

	def 'Test the createFormatterForType with various options'() {
		setup:
			String username = 'UserName7!'

		expect: """Iterating through the various date/datetime formats that the createFormatterForType method returns
				a valid DateFormat object for both the middle-endian and little-endian date formats """

			( TimeUtil.createFormatterForType(TimeUtil.MIDDLE_ENDIAN, formatName) instanceof DateFormat) 			
			( TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, formatName) instanceof DateFormat) 

		where:
			formatName                   | formatValue
			TimeUtil.FORMAT_DATE         | "MM/dd/yyyy"
			TimeUtil.FORMAT_DATE_TIME    | "MM/dd/yyyy hh:mm a"
			TimeUtil.FORMAT_DATE_TIME_2  | "MM-dd-yyyy hh:mm:ss a"
			TimeUtil.FORMAT_DATE_TIME_3  | "E, d MMM 'at ' HH:mma"
			TimeUtil.FORMAT_DATE_TIME_4  | "MM/dd kk:mm"
			TimeUtil.FORMAT_DATE_TIME_5  | "yyyyMMdd"
			TimeUtil.FORMAT_DATE_TIME_6  | "yyyy-MM-dd"
			TimeUtil.FORMAT_DATE_TIME_7  | "dd-MMM"
			TimeUtil.FORMAT_DATE_TIME_8  | "MMM dd,yyyy hh:mm a"
			TimeUtil.FORMAT_DATE_TIME_9  | "MM-dd-yyyy hh:mm a"
			TimeUtil.FORMAT_DATE_TIME_10 | "MMM dd"
			TimeUtil.FORMAT_DATE_TIME_11 | "yyyy/MM/dd hh:mm:ss a"
			TimeUtil.FORMAT_DATE_TIME_12 | "MM-dd-yyyy"
			TimeUtil.FORMAT_DATE_TIME_13 | "MM/dd kk:mm:ss"
			TimeUtil.FORMAT_DATE_TIME_14 | "yyyy-MM-dd hh:mm" //Used in
			TimeUtil.FORMAT_DATE_TIME_15 | "yyyy-MM-dd HH:mm:ss" //Used
			TimeUtil.FORMAT_DATE_TIME_16 | "yyyy-MM-dd hh:mm a" //Used 
			TimeUtil.FORMAT_DATE_TIME_17 | "MM/dd"
			TimeUtil.FORMAT_DATE_TIME_18 | "M/d"
			TimeUtil.FORMAT_DATE_TIME_19 | "M/d kk:mm"
			TimeUtil.FORMAT_DATE_TIME_20 | "hh:mm"
			TimeUtil.FORMAT_DATE_TIME_21 | "mm/dd"
			TimeUtil.FORMAT_DATE_TIME_22 | "MM/dd/yyyy hh:mm:ss a"
			TimeUtil.FORMAT_DATE_TIME_23 | "MM/dd/yy"
			TimeUtil.FORMAT_DATE_TIME_24 | "MM/dd/yyyy hh:mm:ss"
			TimeUtil.FORMAT_DATE_TIME_25 | "MM/dd/yyyy hh:mm"		 
	}

	//
	// formatDate tests
	//F

	def 'Test formatDate(HttpSession session, Date dateValue) and formatDate(Date dateValue, DateFormat formatter)'() {
		// No signature of method: static com.tdssrc.grails.TimeUtil.formatDate() is applicable for argument types: 
		// (org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession, java.sql.Timestamp)
		// TM-4795
		setup:
			// Mock the bullshit format of session attributes ...
			def mockSession = new GrailsHttpSession(request)
			mockSession.setAttribute('CURR_DT_FORMAT', [ 'CURR_DT_FORMAT': TimeUtil.MIDDLE_ENDIAN ] )
			mockSession.setAttribute('CURR_TZ', [ 'CURR_TZ': 'GMT' ] )

			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			long oneDay = 60*60*24*1000
			Timestamp ts = new Timestamp(oneDay)
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)
		expect:
			TimeUtil.formatDate(mockSession, ts) == '01/02/1970'
			TimeUtil.formatDate(ts, formatter) == '02/01/1970'
	}

	def 'Test formatDate(String tzId, Date dateValue, DateFormat formatter)'() {
		setup:
			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			long oneDay = 60*60*24*1000
			Timestamp timestamp = new Timestamp(oneDay).clearTime()
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)
		expect:
			TimeUtil.formatDate('GMT', timestamp, formatter) == '02/01/1970'
	}
	
	def 'Test formatDateTime(HttpSession session, dateValue, DateFormat formatter)'() {
		setup:
			// Mock the bullshit format of session attributes ...
			def mockSession = new GrailsHttpSession(request)
			mockSession.setAttribute('CURR_DT_FORMAT', [ 'CURR_DT_FORMAT': TimeUtil.MIDDLE_ENDIAN ] )
			mockSession.setAttribute('CURR_TZ', [ 'CURR_TZ': 'GMT' ] )
		when:
			TimeUtil.formatDateTime(mockSession, new Date(), null)
		then: 'Make sure that a null formatter causes an exception'
			// thrown(InvalidParamException)
			thrown(RuntimeException)
	}

	def 'Test formatDateTime(String tzId, Date dateValue, DateFormat formatter)'() {
		setup:
			// Timestamp at epoch should be January 1, 1970, 00:00:00 GMT + 1 day
			long oneDay = 60*60*24*1000
			Timestamp timestamp = new Timestamp(oneDay).clearTime()
			def formatter = TimeUtil.createFormatterForType(TimeUtil.LITTLE_ENDIAN, TimeUtil.FORMAT_DATE)
		expect:
			TimeUtil.formatDateTime('GMT', timestamp, formatter) == '02/01/1970'
	}

	def 'Test formatDateTimeWithTZ(String tzId, dateValue, DateFormat formatter)'() {
		when:
			TimeUtil.formatDateTime('GMT', new Date(), null)
		then: 'Make sure that a null formatter causes an exception'
			// thrown(InvalidParamException)
			thrown(RuntimeException)
	}
}