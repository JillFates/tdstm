import com.tdssrc.grails.DateUtil as DU
import static java.util.Calendar.OCTOBER
import java.text.SimpleDateFormat

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the DateUtil class
 */
class DateUtilTests extends Specification {
	
	def testDate

	protected void setup() {
		testDate = new Date()
		testDate.set([year: 2014, month: OCTOBER, date: 5])
		println "testDate = $testDate"
		testDate.clearTime()
		println "testDate = $testDate"
	}

	protected void cleanup() {
	}

	void testMmddyyyy() {
		def formatter = new SimpleDateFormat("M-d-yyyy")
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"))
		def validDate = formatter.format(testDate)
		def formattedDate = formatter.format(DU.parseDate('10/5/2014'))

		expect:
			// 10/5/2014
			validDate == formattedDate
			// Should be null
			DU.parseDate('30/2014') == null
			DU.parseDate('11/30/114') == null
			DU.parseDate('') == null
	}

}