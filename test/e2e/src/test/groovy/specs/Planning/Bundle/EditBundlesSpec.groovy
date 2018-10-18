package specs.Planning.Bundle

/**
 * This Spec tests the edition of bundles
 * @author ingrid
 */

import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Planning.Bundle.ListBundlesPage
import pages.Planning.Bundle.EditBundlePage
import pages.Planning.Bundle.BundleDetailPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class EditBundlesSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    static randStr = CommonActions.getRandomString()
    static bundleData = [baseName+" "+randStr+" Planning", baseName+" bundle created by automated test",
                         "STD_PROCESS","on"]
    static  originalBundleData =[]

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
        given: 'The User has clickes on Bundle detail'
            at ListBundlesPage
            filterByName baseName
            selectFilter()
        when: 'The user clciks on Bundle list'
            clickOnBundle()
        then: 'The User is led to the Create Bundle Page'
            at BundleDetailPage
    }

    def "2. The user has reached the bundle Creation Page and all the expected fields are displayed"(){
        given: 'The user goes back to list bundles and clicks ona bundle'
            goToListbundles()
            at ListBundlesPage
            filterByName baseName
            selectFilter()
            clickOnBundle()
            at BundleDetailPage
            originalBundleData = getDataDisplayed()
        when: 'The user clicks on Edit'
            clickEdit()
        then: 'The Bundle edition page is displayed'
            at EditBundlePage
    }

    def "3. The user cancels bundle edition"(){
        given: 'The user edits the bundle data'
            editName("Edited")
            editDescription("Edited")
            changeIsPlanningValue()
        when: 'The user cancels the edition'
            clickCancel()
        then: 'The user is led to Bundle detail page'
            at BundleDetailPage
        and: 'The changes have not been applied to the bundle'
            validateDataDisplayed(originalBundleData)
    }

    def "4. The user Edits the  bundle"(){
        given: 'The user edits the bundle data'
            clickEdit()
            at EditBundlePage
            editName("Edited")
            editDescription("Edited")
            changeIsPlanningValue()
            def newDates = editDates()
            originalBundleData.add(newDates[0])
            originalBundleData.add(newDates[1])
        when: 'The user saves changes'
            clickSave()
        then: 'The user is led to Bundle detail page'
            at BundleDetailPage
        and: 'The changes have been saved'
            dataIsEdited(originalBundleData)
        and: 'The corresponding message is displayed'
            validateUpdateMesage(originalBundleData[0]+" Edited")
    }

}