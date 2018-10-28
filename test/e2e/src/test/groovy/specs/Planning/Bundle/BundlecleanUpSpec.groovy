package specs.Planning.Bundle

/**
 * This Spec cleans up bundles so that only one planning and one non planning bundle remain.
 * @author ingrid
 */

import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Planning.Bundle.ListBundlesPage
import pages.Planning.Bundle.BundleDetailPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class BundleCleanUpSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2Ea"
    static randStr = CommonActions.getRandomString()
    static bundleData = [baseName + " " + randStr + " Planning", baseName + " bundle created by automated test",
                         "STD_PROCESS", "on"]
    static bundleName = ""
    static proceed = true
    static maxNumberOfBundles = 1

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        planningModule.goToListBundles()
        at ListBundlesPage

    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def deleteBundle(isPlanning) {
        clickOnBundle()
        at BundleDetailPage
        confirmDeletion()
        at ListBundlesPage
        filterByName baseName
        selectFilter()
        if (isPlanning) {
            clickPlanningFilter()
        } else {
            clickNonPlanningFilter()
        }
    }

    def deleteBundles(isPlanning = false) {
        if (numberOfRows()>0){
            while (verifyRowsDisplayed() && numberOfRows() > maxNumberOfBundles) {
                deleteBundle(isPlanning)
            }
        }else{
            println "There are no bundles to work with"
        }
        true // return true to avoid condition fails
    }

    def "1. Planning Bundle cleanup"() {
        given: 'The user is at bundle List page'
            at ListBundlesPage
        when: 'The user filters QAE2E bundles'
            filterByName baseName
            selectFilter()
            clickPlanningFilter()
        then: 'The user deletes planning bundles if  more than one is listed'
            deleteBundles(true)
     }

    def "2. Non-Planning Bundle cleanup"() {
        given: 'The user is at bundle List page'
            at ListBundlesPage
        when: 'The user filters QAE2E NON planning bundles'
            filterByName baseName
            selectFilter()
            clickNonPlanningFilter()
        then: 'If more than 2 non-planning bundles are listed, the excess bundles are deleted'
            deleteBundles(false)
    }
}