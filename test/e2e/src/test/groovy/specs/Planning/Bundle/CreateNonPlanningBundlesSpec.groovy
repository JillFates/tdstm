package specs.Planning.Bundle

/**
 * This Spec tests the creation of a non planning bundle and the validation of the creation form fields
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

// import geb.driver.CachingDriverFactory

@Stepwise
class CreateNonPlanningBundlesSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    static randStr = CommonActions.getRandomString()
    static bundleData = [baseName+" "+randStr+" NON-Planning", baseName+" bundle created by automated test",
                         "STD_PROCESS",false]

    def setupSpec() {
        // CachingDriverFactory.clearCacheAndQuitDriver()

        testCount = 0
        to LoginPage
        login()
        at MenuPage
        sleep(1000)
        planningModule.goToListBundles()
        sleep(5000)
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user has access to Bundle List"() {
        given: 'The User is at Bundle List'
            at ListBundlesPage
        when: 'The user clicks Create button'
            clickCreate()
        then: 'The User is led to the Create Bundle Page'
            at CreateBundlePage
    }

    def "2. The user has reached the bundle Creation Page and all the expected fields are displayed"(){
        when: 'The user is at Bundle creation Page'
            at CreateBundlePage
        then: 'The Planning bundle checkbox is unchecked'
            !isPlanning()
        and: 'The Save button is disabled'
            validateSaveIsDisabled()
        and: 'All fields are present'
            validatePresentFields()

    }

    def "3. The user sees in the list the bundle they have just created"(){
        given: 'The user has entered all mandatory data and unchecked the planning checkbox'
            clearName()
            enterBundleData bundleData
        when: 'The user clicks save'
            clickSave()
        then: 'The Bundle List Page is displayed'
            at ListBundlesPage

        when: 'The data displayed is the data entered by the user'
            filterByName(bundleData[0])
            clickOnBundle()
            at BundleDetailPage
        then:
            validateDataDisplayed bundleData
    }

    def "4. The User is able to filter the newly created bundle in Bundle list page"(){
        given: 'The user goes to Bundle List'
            closeModal()
            at ListBundlesPage
        when: 'The user filters the newly created bundle'
            filterByName bundleData[0]
        then: 'Data is correctly displayed'
            validateBundleRowData bundleData
    }

}
