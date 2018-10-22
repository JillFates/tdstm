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

@Stepwise
class CreateNonPlanningBundlesSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    static randStr = CommonActions.getRandomString()
    static bundleData = [baseName+" "+randStr+" Planning", baseName+" bundle created by automated test",
                         "STD_PROCESS",false]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
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
        then: 'All expected fields are displayed'
            validatePresentFields()
        and: 'The Planning bundle checkbox is checked'
            isPlanning()
    }

    def "3. The user is unable to save a bundle without a name"(){
        when: 'The user clicks on save without having entered a name'
            clickSave()
        then: 'Saving is not allowed and the corresponding message is displayed'
            validateNameMessage()
    }

    def "4. the user adds a name and is unable to save without Workflow"(){
        given: 'The user has enterd a name for the bundle'
            enterName(bundleData[0])
        when: 'The user clicks on Save'
            clickSave()
        then:'The bundle is not saved and a message stating Move bundle cannot be blank is displayed'
            validateWorkFlowMessage()
    }

    def "5. The user is led to Bundle detail landing once the bundle is created"(){
        given: 'The user has entered all mandatory data and unchecked the planning checkbox'
            clearName()
            enterBundleData bundleData
            clickPlanning()
        when: 'The user clicks save'
            clickSave()
        then: 'The Bundle detail Landing page is displayed'
            at BundleDetailPage
        and: 'The data displayed is the data entered by the user'
            validateDataDisplayed bundleData
        and: 'The bundle creation message is displayed'
            bundleCreatedMsgIsDisplayed bundleData
        and: 'The bundle name is displayed in the page header'
            menuModule.assertBundleName bundleData[0]
    }

    def "6. The recently created Bundle's name is displayed in the Planning menu"(){
        when: 'The user clicks on the planning menu'
            planningModule.goToPlanningMenu()
        then: 'The name of the just created bundle is displayed'
            planningModule.vaildateDisplayedBundleName bundleData[0]
    }

    def "7. The User is able to filter the newly created bundle in Bundle list page"(){
        given: 'The user goes to Bundle List'
            planningModule.goToListBundles()
            at ListBundlesPage
        when: 'The user filters the newly created bundle'
            filterByName bundleData[0]
        then: 'Data is correctly displayed'
            validateBundleRowData bundleData
    }

}
