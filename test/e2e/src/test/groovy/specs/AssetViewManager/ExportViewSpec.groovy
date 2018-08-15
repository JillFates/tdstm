package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Ignore
import pages.AssetViewManager.AssetViewsPage
import pages.AssetViewManager.ViewPage
import utils.CommonActions
import pages.Downloads.HomeUserDownloadsPage

@Ignore
class ExportViewSpec extends GebReportingSpec {

    def testKey
    static testCount

    //Define the names of the Application you will Create and Edit
    static randStr = CommonActions.getRandomString()
    static fileName = "QAE2E " + randStr  + " ExportSystemAllAssets"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { menuModule.goToAssetViewManager() }
    }

    def "1. User loads the system 'All Views' view"() {
        given: "The user is in View Manager Page"
            at AssetViewsPage
        when: "The user clicks on System Views"
            goToSystemViews()
        and: "The User opens 'All Assets' view"
            allViewsModule.openViewByName "All Assets"
        then: "'All Assets' view should be displayed"
            at ViewPage
            verifyViewTitle "All Assets"
    }

    def "2. The User cancels export system 'All Views' view process"() {
        given: "The User is on 'All Views' view Page"
            at ViewPage
        when: 'The User clicks on Export view button'
            clickOnExportViewButton()
        and: 'The user waits the modal displayed'
            waitForDisplayedModalContainer()
        and: 'The user clicks on Cancel button'
            clickOnCancelModalButton()
        then: 'Export modal is hidden'
            waitForHiddenModalContainer()
    }

    def "3. The User exports the system 'All Views' view"() {
        given: "The User is on 'All Views' view Page"
            at ViewPage
        when: 'The User clicks on Export view button'
            clickOnExportViewButton()
        and: 'The user waits the modal displayed'
            waitForDisplayedModalContainer()
            commonsModule.waitForLoader()
        and: 'The user sets a random file name'
            setExportFileName fileName
        and: 'The user clicks on Export button'
            clickOnExportModalButton()
        then: 'Export modal is hidden'
            waitForHiddenModalContainer()
    }

    def "4. Verify the system 'All Views' view starts downloading"() {
        given: "The User is on 'All Views' view Page"
            at ViewPage
        when: 'The user browse to Downloads folder'
            to HomeUserDownloadsPage
        then: 'Export modal is hidden'
            waitForExportedFile fileName
    }
}