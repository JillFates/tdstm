package specs.LandingPages

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Planning.Bundle.*
import pages.Planning.*
import pages.Planning.Events.*
import spock.lang.Stepwise

import geb.driver.CachingDriverFactory

/**
 * This class sweeps over the Planning Menu.
 * It checks every single landing page.
 * @author alvaro
 */

@Stepwise
class PlanningLandingPageSpec extends GebReportingSpec {
    def testKey
    static testCount

    def setupSpec() {
        CachingDriverFactory.clearCacheAndQuitDriver()
        
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

    def "1. The Planning has the correct number of elements"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The User Clicks on the Planning Menu'
            planningModule.goToPlanningMenu()

        then: 'The correct number of items are displayed'
            planningModule.planningMenu.children().size() == 6
            at MenuPage
    }

    def "2. Verify the List Events Landing Page"() {
        when: 'The user goes to the List Events page'
            planningModule.goToListEvents()

        then: 'The List Events Page is loaded successfully'
            at ListEventsPage
    }

    def "3. Verify the List Event News Landing Page"() {
        when: 'The user goes to the List Event News page'
            planningModule.goToListEventNews()

        then: 'The List Event News Page is loaded successfully'
            at ListEventNewsPage
    }

    def "4. Verify the Pre-event Checklist Landing Page"() {
        when: 'The user goes to the Pre-event Checklist page'
            planningModule.goToPreEventChecklist()

        then: 'The Pre-event Checklist Page is loaded successfully'
            at PreEventChecklistPage
    }

    def "5. Verify the Export Runbook Landing Page"() {
        when: 'The user goes to the Export Runbook page'
            planningModule.goToExportRunbook()

        then: 'The Export Runbook Page is loaded successfully'
            at ExportRunbookPage
    }

    def "6. Verify the List Bundles Landing Page"() {
        when: 'The user goes to the List Bundles page'
            planningModule.goToListBundles()

        then: 'The List Bundles Page is loaded successfully'
            at ListBundlesPage
    }
}
