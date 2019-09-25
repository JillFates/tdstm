package com.tdssrc.grails

import grails.util.Pair
import net.transitionmanager.exception.InvalidParamException
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

class DateTimeFilterUtilSpec extends Specification {

	@Shared
	SimpleDateFormat formatter = new SimpleDateFormat("MMM dd hh:mm:ss 'CST' yyyy")


	def setupSpec() {
		println 'Setup'
		// setting clock to 2019-05-31
		//DateTimeUtils.setCurrentMillisFixed(1559282400000)
		DateTimeUtils.setCurrentMillisFixed(new DateTime(2019, 5, 31, 0, 0).getMillis())
		println DateTime.now()
	}

	def cleanupSpec() {
		println 'Clean up'
		// back to normal
		//DateTimeUtils.setCurrentMillisOffset(1559282400000)
		DateTimeUtils.setCurrentMillisSystem()
	}

	private Date toDate(String date) {
		return formatter.parse(date)
	}

	@Unroll
	def 'Test DateTime filter logic for full year'() {
		given:
			Pair<Date, Date> result = DateTimeFilterUtil.parseUserEntry(entry)

		when:
			DateTimeFilterUtil.parseUserEntry('20019')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('= 2019')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=19')
		then:
			thrown(InvalidParamException)

		expect:
			result.getaValue() == a
			result.getbValue() == b

		where:
			entry   | a                                  | b
			'2019'  | toDate('Jan 01 00:00:00 CST 2019') | toDate('Dec 31 23:59:59 CST 2019')
			'=2018' | toDate('Jan 01 00:00:00 CST 2018') | toDate('Dec 31 23:59:59 CST 2018')
	}

	@Unroll
	def 'Test DateTime filter logic for year and month'() {
		given:
			Pair<Date, Date> result = DateTimeFilterUtil.parseUserEntry(entry)
		when:
			DateTimeFilterUtil.parseUserEntry('20019-17')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=20019-13')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=20019-00')
		then:
			thrown(InvalidParamException)
		expect:
			[result.getaValue(), result.getbValue()] == [a, b]
		where:
			entry      | a                                    | b
			'2019-07'  | toDate('Jul 01 00:00:00 CST 2019') | toDate('Jul 31 23:59:59 CST 2019')
			'=2018-07' | toDate('Jul 01 00:00:00 CST 2018') | toDate('Jul 31 23:59:59 CST 2018')
			'=2016-02' | toDate('Feb 01 00:00:00 CST 2016') | toDate('Feb 29 23:59:59 CST 2016')
	}

	@Unroll
	def 'Test DateTime filter logic for year, month and day'() {
		given:
			Pair<Date, Date> result = DateTimeFilterUtil.parseUserEntry(entry)
		when:
			DateTimeFilterUtil.parseUserEntry('2019-13-01')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=2019-13-01')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=2009-00-01')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=2019-07-00')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=2019-02-31')
		then:
			thrown(InvalidParamException)
		expect:
			[result.getaValue(), result.getbValue()] == [a, b]
		where:
			entry         | a                                    | b
			'2019-07-01'  | toDate('Jul 01 00:00:00 CST 2019') | toDate('Jul 01 23:59:59 CST 2019')
			'=2018-07-01' | toDate('Jul 01 00:00:00 CST 2018') | toDate('Jul 01 23:59:59 CST 2018')
			'=2016-02-29' | toDate('Feb 29 00:00:00 CST 2016') | toDate('Feb 29 23:59:59 CST 2016')
	}

	@Unroll
	def 'Test DateTime filter logic for date range'() {
		given:
			Pair<Date, Date> result = DateTimeFilterUtil.parseUserEntry(entry)
		when:
			DateTimeFilterUtil.parseUserEntry('=2019-02-31<>=2019-03-01')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=2019-02-31<>==2019-03-01')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=2019-01-1<>2019')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('2019<>2018')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('2019 <> 2020')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('2019 < > 2020')
		then:
			thrown(InvalidParamException)
		expect:
			[result.getaValue(), result.getbValue()] == [a, b]
		where:
			entry                    | a                                  | b
			'2019<>2020'             | toDate('Jan 01 00:00:00 CST 2019') | toDate('Dec 31 23:59:59 CST 2020')
			'=2018<>2020'            | toDate('Jan 01 00:00:00 CST 2018') | toDate('Dec 31 23:59:59 CST 2020')
			'2019-07<>2019-08'       | toDate('Jul 01 00:00:00 CST 2019') | toDate('Aug 31 23:59:59 CST 2019')
			'2019-07-17<>2019-08-17' | toDate('Jul 17 00:00:00 CST 2019') | toDate('Aug 17 23:59:59 CST 2019')
			'=2019-07-17<>2019-08'   | toDate('Jul 17 00:00:00 CST 2019') | toDate('Aug 31 23:59:59 CST 2019')
			'-2<>3'                  | toDate('May 29 00:00:00 CST 2019') | toDate('Jun 03 23:59:59 CST 2019')
			'-2d<>+3d'               | toDate('May 29 00:00:00 CST 2019') | toDate('Jun 03 23:59:59 CST 2019')
			'-3M<>2w'                | toDate('Feb 28 00:00:00 CST 2019') | toDate('Jun 14 23:59:59 CST 2019')
			'-3M<>+2w'               | toDate('Feb 28 00:00:00 CST 2019') | toDate('Jun 14 23:59:59 CST 2019')
			'-3M<>3'                 | toDate('Feb 28 00:00:00 CST 2019') | toDate('Jun 03 23:59:59 CST 2019')
			'-2<>3d'                 | toDate('May 29 00:00:00 CST 2019') | toDate('Jun 03 23:59:59 CST 2019')
	}

	@Unroll
	def 'Test DateTime filter logic for today'() {
		given:
			Pair<Date, Date> result = DateTimeFilterUtil.parseUserEntry(entry)
		when:
			DateTimeFilterUtil.parseUserEntry('0t')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=0')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=t')
		then:
			thrown(InvalidParamException)
		expect:
			[result.getaValue(), result.getbValue()] == [a, b]
		where:
			entry | a                                    | b
			'0'   | toDate('May 31 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2019')
			't'   | toDate('May 31 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2019')
	}

	@Unroll
	def 'Test DateTime filter logic for day'() {
		given:
			Pair<Date, Date> result = DateTimeFilterUtil.parseUserEntry(entry)
		when:
			DateTimeFilterUtil.parseUserEntry('=2d')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=2')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('-9999')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('-9999d')
		then:
			thrown(InvalidParamException)
		expect:
			[result.getaValue(), result.getbValue()] == [a, b]
		where:
			entry  | a                                    | b
			'-2'   | toDate('May 29 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2019')
			'-2d'  | toDate('May 29 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2019')
			'2'    | toDate('May 31 00:00:00 CST 2019') | toDate('Jun 02 23:59:59 CST 2019')
			'+2d'  | toDate('May 31 00:00:00 CST 2019') | toDate('Jun 02 23:59:59 CST 2019')
			'-90d' | toDate('Mar 02 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2019')
			'-90'  | toDate('Mar 02 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2019')
	}

	@Unroll
	def 'Test DateTime filter logic for week'() {
		given:
			Pair<Date, Date> result = DateTimeFilterUtil.parseUserEntry(entry)
		when:
			DateTimeFilterUtil.parseUserEntry('=3w')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=-3w')
		then:
			thrown(InvalidParamException)
		expect:
			[result.getaValue(), result.getbValue()] == [a, b]
		where:
			entry | a                                    | b
			'3w'  | toDate('May 31 00:00:00 CST 2019') | toDate('Jun 21 23:59:59 CST 2019')
			'+3w' | toDate('May 31 00:00:00 CST 2019') | toDate('Jun 21 23:59:59 CST 2019')
			'-3w' | toDate('May 10 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2019')
	}

	@Unroll
	def 'Test DateTime filter logic for month'() {
		given:
			Pair<Date, Date> result = DateTimeFilterUtil.parseUserEntry(entry)
		when:
			DateTimeFilterUtil.parseUserEntry('=3M')
		then:
			thrown(InvalidParamException)
		when:
			DateTimeFilterUtil.parseUserEntry('=-3M')
		then:
			thrown(InvalidParamException)
		expect:
			[result.getaValue(), result.getbValue()] == [a, b]
		where:
			entry | a                                    | b
			'3M'  | toDate('May 31 00:00:00 CST 2019') | toDate('Aug 31 23:59:59 CST 2019')
			'+3M' | toDate('May 31 00:00:00 CST 2019') | toDate('Aug 31 23:59:59 CST 2019')
			'-3M' | toDate('Feb 28 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2019')
			'12M' | toDate('May 31 00:00:00 CST 2019') | toDate('May 31 23:59:59 CST 2020')
	}

}
