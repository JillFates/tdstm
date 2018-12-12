package specs.LandingPages

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Reports.*
import spock.lang.Stepwise

/**
 * This class sweeps over the Reports Menu.
 * It checks every single landing page.
 * @author alvaro
 */

@Stepwise
class ReportsLandingPageSpec extends GebReportingSpec {
    def testKey
    static testCount

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The Reports Menu has the correct number of elements"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The User Clicks on the Reports Menu'
            reportsModule.goToReportsMenu()

        then: 'The valid clickable pages are displayed'
            reportsModule.reportsPages.size() == 7
            at MenuPage
    }

    def "2. Verify the Application Profiles Landing Page"() {
        when: 'The user goes to the Application Profiles page'
            reportsModule.goToApplicationProfiles()

        then: 'The Application Profiles Page is loaded successfully'
            at ApplicationProfilesPage
    }

    def "3. Verify the Application Conflicts Landing Page"() {
        when: 'The user goes to the Application Conflicts page'
            reportsModule.goToApplicationConflicts()

        then: 'The Application Conflicts Page is loaded successfully'
            at ApplicationConflictsPage
    }

    def "4. Verify the Server Conflicts Landing Page"() {
        when: 'The user goes to the Server Conflicts page'
            reportsModule.goToServerConflicts()

        then: 'The Server Conflicts Page is loaded successfully'
            at ServerConflictsPage
    }

    def "5. Verify the Database Conflicts Landing Page"() {
        when: 'The user goes to the Database Conflicts page'
            reportsModule.goToDatabaseConflicts()

        then: 'The Database Conflicts Page is loaded successfully'
            at DatabaseConflictsPage
    }

    def "6. Verify the Task Report Landing Page"() {
        when: 'The user goes to the Task Report page'
            reportsModule.goToTaskReport()

        then: 'The Task Report Page is loaded successfully'
            at TaskReportPage
    }

    def "7. Verify the Activity Metrics Landing Page"() {
        when: 'The user goes to the Activity Metrics page'
            reportsModule.goToActivityMetrics()

        then: 'The Activity Metrics Page is loaded successfully'
            at ActivityMetricsPage
    }

    def "8. Verify the Application Event Results Page"() {
        when: 'The user goes to the Application Event Results page'
            reportsModule.goToApplicationEventResults()

        then: 'The Application Event Results Page is loaded successfully'
            at ApplicationEventResultsPage
    }

}
