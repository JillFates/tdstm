package specs.Planning.Bundle

/**
 * This Spec test bndle deleta and cancel delete for both planning and non planning bundles
 * @author ingrid
 */

import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Planning.Bundle.ListBundlesPage
import pages.Planning.Bundle.CreateBundlePage
import pages.Planning.Bundle.BundleDetailPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class BundleDeleteSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    static randStr = CommonActions.getRandomString()
    static bundleData1 = [baseName + " " + randStr + " Planning", baseName + " bundle created by automated test",
                          "STD_PROCESS", "on"]
    static bundleData2 = [baseName+" "+randStr+" NON-Planning", baseName+" bundle created by automated test",
                         "STD_PROCESS",false]
    static bundleName = ""
    static proceed = true
    static maxNumberOfBundles = 2

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        planningModule.goToListBundles()
        at ListBundlesPage
        // create a planning bundle to be deleted
        clickCreate()
        at CreateBundlePage
        enterBundleData bundleData1
        clickSave()
        at BundleDetailPage
        at MenuPage
        planningModule.goToListBundles()
        // create a non planning bundle to be deleted
        at ListBundlesPage
        clickCreate()
        at CreateBundlePage
        enterBundleData bundleData2
        clickPlanning() // make it non planning
        clickSave()
        at BundleDetailPage
        at MenuPage
        planningModule.goToListBundles()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }


    def "1. Test planning bundles cancelled deletion attempt"() {
        given: 'The user is in a planning bundle detail page'
            at ListBundlesPage
            filterByName bundleData1[0]
            selectFilter()
            clickPlanningFilter()
            clickOnBundle()
            at BundleDetailPage
            bundleName=getDataDisplayed()[0]
        when: 'The user clicks on Delete and cancels'
            cancelDeletion()
        then: 'The bundle is listed in Bundle list Page'
            goToListbundles()
            at ListBundlesPage
            validateBundleIsListed(bundleName)
    }

    def "2. Bundle deletion is confirmed"(){
        given: 'The user is in a planning bundle detail page'
            at ListBundlesPage
            filterByName bundleData1[0]
            selectFilter()
            clickPlanningFilter()//if i get at least two rows i can proceed
            clickOnBundle()
            at BundleDetailPage
        when: 'The user clicks on Cancel and confirms'
            confirmDeletion()
        then: 'The bundle is not deleted'
            at ListBundlesPage
            filterByName bundleData1[0]
            selectFilter()
            clickPlanningFilter()
            !validateBundleIsListed(bundleName)
    }

    def "3. The user filters non-planning bundles"() {
        given: 'The user is in a non-planning bundle detail page'
            at ListBundlesPage
            filterByName bundleData2[0]
            selectFilter()
            clickNonPlanningFilter()
            clickOnBundle()
            at BundleDetailPage
            bundleName=getDataDisplayed()[0]
        when: 'The user clicks on Delete and cancels'
            cancelDeletion()
        then: 'The bundle is listed in Bundle list Page'
            goToListbundles()
            at ListBundlesPage
            validateBundleIsListed(bundleName)
    }

    def "4. Bundle deletion is confirmed"(){
        given: 'The user is in a planning bundle detail page'
            at ListBundlesPage
            filterByName bundleData2[0]
            selectFilter()
            clickNonPlanningFilter()
            clickOnBundle()
            at BundleDetailPage
        when: 'The user clicks on Cancel and confirms'
            confirmDeletion()
        then: 'The bundle is not deleted'
            at ListBundlesPage
            filterByName bundleData2[0]
            selectFilter()
            clickPlanningFilter()
            !validateBundleIsListed(bundleName)
    }
}