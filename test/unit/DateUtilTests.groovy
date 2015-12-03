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

    // Utility method to create a date that had time set to 00:00:00
    protected Date createDate(year, month, day) {
        Date d = new Date()
        d.set([year:year, month:month, date: day])
        d.clearTime()
        return d
    }

    void testMmddyyyy() {
        assertEquals '10/5/2014', testDate, DU.mdyToDate('10/5/2014')
        assertEquals '10/05/2014', testDate, DU.mdyToDate('10/05/2014')
        assertNull DU.mdyToDate('19/7/2014')
        assertNull DU.mdyToDate('2/30/2014')
        assertNull DU.mdyToDate('')
        assertNull DU.mdyToDate(null)
    }

    void testParseDate() {
        assertEquals  'case 1', createDate( 2015, OCTOBER, 4), DU.parseDate('10/4/15')
        assertEquals  'case 2', createDate( 2015, OCTOBER, 4), DU.parseDate('10/4/2015')
        assertEquals  'case 3', createDate( 2015, OCTOBER, 4), DU.parseDate('10/04/15')
        assertEquals  'case 4', createDate( 2015, OCTOBER, 4), DU.parseDate('10/04/2015')
        assertEquals  'case 5', createDate( 2015, OCTOBER, 4), DU.parseDate('10-4-15')
        assertEquals  'case 6', createDate( 2015, OCTOBER, 4), DU.parseDate('10-4-2015')
        assertEquals  'case 7', createDate( 2015, OCTOBER, 4), DU.parseDate('10-04-15')
        assertEquals  'case 8', createDate( 2015, OCTOBER, 4), DU.parseDate('10-04-2015')
        assertNull    'blank', DU.parseDate('')
        assertNull    'null', DU.parseDate(null)

        try {
            def d = DU.parseDate('abc123')
            assertTrue "parseDate('abc123') didn't throw exception, instead got $d", false
        } catch (e) {
            // As expected
        }

        try {
            def d = DU.parseDate('a/40/2015')
            assertTrue "parseDate('a/40/2015') didn't throw exception, instead got $d", false
        } catch (e) {
            // As expected
        }
    }

}