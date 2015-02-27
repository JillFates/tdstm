import com.tdssrc.grails.DateUtil as DU
import static java.util.Calendar.OCTOBER

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
		expect:
			// 10/5/2014
			testDate == DU.mdyToDate('10/5/2014')
			// 10/05/2014
			testDate == DU.mdyToDate('10/05/2014')
			// Should be null
			DU.mdyToDate('19/7/2014') == null
			DU.mdyToDate('2/30/2014') == null
			DU.mdyToDate('') == null
			DU.mdyToDate(null) == null
	}

}