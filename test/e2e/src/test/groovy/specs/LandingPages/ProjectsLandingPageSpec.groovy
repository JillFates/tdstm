package specs.LandingPages

import geb.spock.GebReportingSpec
import pages.Projects.*
import pages.Providers.*
import pages.Tags.*
import pages.Datascripts.*
import pages.Credentials.*
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

/**
 * This class sweeps over the Projects Menu.
 * It checks every single landing page.
 * @author alvaro
 */

@Stepwise
class ProjectsLandingPageSpec extends GebReportingSpec {
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

    def "1. The Projects Menu has the correct number of elements"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The User Clicks in the Projects Menu'
            projectsModule.goToProjectsMenu()

        then: 'The valid clickable pages are displayed'
            projectsModule.projectsPages.size() == 10
            at MenuPage
    }

    def "2. Verify the Active Projects Landing Page"() {
        when: 'The user goes to the Active Projects page'
            projectsModule.goToProjectsActive()

        then: 'The Active Projects Page is loaded successfully'
            at ProjectListPage
    }

    def "3. Verify the Project Details Landing Page"() {
        when: 'The user goes to the Project Details page'
            projectsModule.goToProjectsDetails()

        then: 'The PProject Details Page is loaded successfully'
            at ProjectDetailsPage
    }

    def "4. Verify the Projects Staff Landing Page"() {
        when: 'The user goes to the Projects Staff page'
            projectsModule.goToProjectsStaff()

        then: 'The Projects Staff Page is loaded successfully'
            at ProjectStaffPage
    }

    def "5. Verify the User Activation Emails Landing Page"() {
        when: 'The user goes to the User Activation Emails page'
            projectsModule.goToUserActivationEmails()

        then: 'The User Activation Emails Page is loaded successfully'
            at UserActivationEmailsPage
    }

    def "6. Verify the Asset Field Settings Landing Page"() {
        when: 'The user goes to the Asset Field Settings page'
            projectsModule.goToAssetFieldSettings()

        then: 'The Asset Field Settings Page is loaded successfully'
            at AssetFieldSettingsPage
    }

    def "7. Verify the Tags Landing Page"() {
        when: 'The user goes to the Asset Field Settings page'
            projectsModule.goToTagsPage()

        then: 'The Tags Page is loaded successfully'
            at TagsPage
    }

    def "8. Verify the Providers Landing Page"() {
        when: 'The user goes to the Asset Field Settings page'
            projectsModule.goToProviders()

        then: 'The Providers Page is loaded successfully'
            at ProvidersPage
    }

    def "9. Verify the Credentials Landing Page"() {
        when: 'The user goes to the Asset Field Settings page'
            projectsModule.goToCredentials()

        then: 'The Credentials Page is loaded successfully'
            at CredentialsPage
    }

    def "10. Verify the ETL Scripts Landing Page"() {
        when: 'The user goes to the ETL Scripts  page'
            projectsModule.goToDatascripts()

        then: 'The ETL Scripts Page is loaded successfully'
            at DatascriptsPage
    }

    def "11. Verify the Actions Landing Page"() {
        when: 'The user goes to Actions page'
            projectsModule.goToActions()

        then: 'The ActionsPage is loaded successfully'
            at ActionsPage
    }

}
