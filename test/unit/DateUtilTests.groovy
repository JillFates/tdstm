import grails.test.*
import com.tdssrc.grails.DateUtil as DU
import static java.util.Calendar.OCTOBER

class DateUtilTests extends GrailsUnitTestCase {
	
    def testDate

    protected void setUp() {
        super.setUp()

        testDate = new Date()
        testDate.set([year: 2014, month: OCTOBER, date: 5])
        println "testDate = $testDate"
        testDate.clearTime()
        println "testDate = $testDate"
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testMmddyyyy() {
        assertEquals '10/5/2014', testDate, DU.mdyToDate('10/5/2014')
        assertEquals '10/05/2014', testDate, DU.mdyToDate('10/05/2014')
        assertNull DU.mdyToDate('19/7/2014')
        assertNull DU.mdyToDate('2/30/2014')
        assertNull DU.mdyToDate('')
        assertNull DU.mdyToDate(null)
    }

}