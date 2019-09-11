package com.tdssrc.grails

import grails.util.Pair
import net.transitionmanager.exception.InvalidParamException
import org.joda.time.DateTimeUtils
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class DateTimeFilterUtilSpec extends Specification {

	def setupSpec() {
		println 'Setup'
		// setting clock to 2019-05-31
		DateTimeUtils.setCurrentMillisFixed(1559282400000)
		Pair<Date, Date> result
	}

	def cleanupSpec() {
		println 'Clean up'
		// back to normal
		DateTimeUtils.setCurrentMillisOffset(1559282400000)
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
			[result.getaValue(), result.getbValue()] == [a, b]
		where:
			entry 	| a 										| b
			'2019'	| new Date('Jan 01 00:00:00 CST 2019') 	| new Date('Dec 31 23:59:59 CST 2019')
			'=2018'	| new Date('Jan 01 00:00:00 CST 2018') 	| new Date('Dec 31 23:59:59 CST 2018')
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
			entry 		| a 										| b
			'2019-07'	| new Date('Jul 01 00:00:00 CST 2019') 	| new Date('Jul 31 23:59:59 CST 2019')
			'=2018-07'	| new Date('Jul 01 00:00:00 CST 2018') 	| new Date('Jul 31 23:59:59 CST 2018')
			'=2016-02'	| new Date('Feb 01 00:00:00 CST 2016') 	| new Date('Feb 29 23:59:59 CST 2016')
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
			entry 			| a 										| b
			'2019-07-01'	| new Date('Jul 01 00:00:00 CST 2019') 	| new Date('Jul 01 23:59:59 CST 2019')
			'=2018-07-01'	| new Date('Jul 01 00:00:00 CST 2018') 	| new Date('Jul 01 23:59:59 CST 2018')
			'=2016-02-29'	| new Date('Feb 29 00:00:00 CST 2016') 	| new Date('Feb 29 23:59:59 CST 2016')
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
			entry 						| a											| b
			'2019<>2020'				| new Date('Jan 01 00:00:00 CST 2019') 	| new Date('Dec 31 23:59:59 CST 2020')
			'=2018<>2020'				| new Date('Jan 01 00:00:00 CST 2018') 	| new Date('Dec 31 23:59:59 CST 2020')
			'2019-07<>2019-08'			| new Date('Jul 01 00:00:00 CST 2019') 	| new Date('Aug 31 23:59:59 CST 2019')
			'2019-07-17<>2019-08-17'	| new Date('Jul 17 00:00:00 CST 2019') 	| new Date('Aug 17 23:59:59 CST 2019')
			'=2019-07-17<>2019-08'		| new Date('Jul 17 00:00:00 CST 2019') 	| new Date('Aug 31 23:59:59 CST 2019')
			'-2<>3'						| new Date('May 29 00:00:00 CST 2019') 	| new Date('Jun 03 23:59:59 CST 2019')
			'-2d<>+3d'					| new Date('May 29 00:00:00 CST 2019') 	| new Date('Jun 03 23:59:59 CST 2019')
			'-3M<>2w'					| new Date('Feb 28 00:00:00 CST 2019') 	| new Date('Jun 14 23:59:59 CST 2019')
			'-3M<>+2w'					| new Date('Feb 28 00:00:00 CST 2019') 	| new Date('Jun 14 23:59:59 CST 2019')
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
			entry 		| a 										| b
			'0'			|new Date('May 31 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2019')
			't'			|new Date('May 31 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2019')
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
			entry	 	| a 										| b
			'-2' 		|new Date('May 29 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2019')
			'-2d'		|new Date('May 29 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2019')
			'2'			|new Date('May 31 00:00:00 CST 2019') 	| new Date('Jun 02 23:59:59 CST 2019')
			'+2d'		|new Date('May 31 00:00:00 CST 2019') 	| new Date('Jun 02 23:59:59 CST 2019')
			'-90d'		|new Date('Mar 02 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2019')
			'-90'		|new Date('Mar 02 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2019')
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
			entry	 	| a 										| b
			'3w' 		|new Date('May 31 00:00:00 CST 2019') 	| new Date('Jun 21 23:59:59 CST 2019')
			'+3w'		|new Date('May 31 00:00:00 CST 2019') 	| new Date('Jun 21 23:59:59 CST 2019')
			'-3w' 		|new Date('May 10 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2019')
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
			entry	 	| a 										| b
			'3M' 		|new Date('May 31 00:00:00 CST 2019') 	| new Date('Aug 31 23:59:59 CST 2019')
			'+3M'		|new Date('May 31 00:00:00 CST 2019') 	| new Date('Aug 31 23:59:59 CST 2019')
			'-3M' 		|new Date('Feb 28 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2019')
			'12M' 		|new Date('May 31 00:00:00 CST 2019') 	| new Date('May 31 23:59:59 CST 2020')
	}

}
