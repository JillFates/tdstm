package specs.Assets

import geb.spock.GebReportingSpec
import pages.Assets.AssetExportPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.Downloads.HomeUserDownloadsPage

@Stepwise
class AssetExportSpec extends GebReportingSpec {
    def testKey
    static testCount
    static projName = "TM-Demo"
    static fileName = "TM_Demo-${projName}-"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        assert menuModule.assertProjectName(projName), "${projName} is required to perform this test"
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The User Navigates in the Asset Export Section"() {
        testKey = "TM-8478"
        given: 'The User searches in the Menu Page'
            at MenuPage
        when: 'The User Clicks in the Assets > Asset Export Menu Option'
            menuModule.goToAssetExport()
        then: 'Export Accounts page should be displayed'
            at AssetExportPage
    }

    def "2. The User exports user accounts"() {
        testKey = "TM-8478"
        given: 'The User is on the Export Accounts Page'
            at AssetExportPage
        when: 'The User selects a bundle type'
            randomSelectBundle()
        and: 'The user selects random items to be exported'
            randomChooseItemsToExport()
        and: 'The user clicks on export button'
            clickOnExportExcel()
            commonsModule.waitForGlobalProgressBarModal()
        then: 'The user browse to Downloads folder'
            to HomeUserDownloadsPage
        and: 'The file started download process'
            waitForExportedFile fileName
    }
}