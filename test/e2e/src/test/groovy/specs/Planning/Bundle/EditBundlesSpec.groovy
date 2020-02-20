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
import pages.Planning.Bundle.CreateBundlePage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import utils.CommonActions

// import geb.driver.CachingDriverFactory

@Stepwise
class EditBundlesSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    static randStr = CommonActions.getRandomString()
    static bundleData = [baseName+" "+randStr+" Planning", baseName+" bundle created by automated test",
                         "STD_PROCESS","false"]
    static  originalBundleData =[]

    def setupSpec() {
        // CachingDriverFactory.clearCacheAndQuitDriver()

        testCount = 0
        to LoginPage
        login()
        sleep(3000)
        at MenuPage
        sleep(1500)
        planningModule.goToListBundles()
        sleep(1500)
        at ListBundlesPage
        clickCreate()
        at CreateBundlePage
        enterBundleData bundleData

        clickSave()

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
            filterByName bundleData[0]
        when: 'The user clicks on Bundle list'
            clickOnBundle()

        then: 'The User is led to the Bundle Detail Page'
            at BundleDetailPage
    }

    def "2. The user has reached the bundle Creation Page and all the expected fields are displayed"(){
        given: 'The user goes back to list bundles and clicks ona bundle'
            closeModal()
            at ListBundlesPage
            filterByName bundleData[0]
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
            cancelEdition()
            clickClose()
            at ListBundlesPage
            filterByName(bundleData[0])
            clickOnBundle()
            at BundleDetailPage
            sleep(2000)
        then: 'The changes have not been applied to the bundle'
            validateNameDescription(bundleData)
    }

    def "4. The user Edits the  bundle"(){
        given: 'The user edits the bundle data'
            clickEdit()
            at EditBundlePage
            editName("Edited")
            editDescription("Edited")
        when: 'The user saves changes'
            clickSave()
            at BundleDetailPage
            closeModal()
            at ListBundlesPage
            filterByName(bundleData[0])
            clickOnBundle()
            at BundleDetailPage

        then: 'The changes have been saved'
            nameDescriptionEdited(originalBundleData)

    }

}
