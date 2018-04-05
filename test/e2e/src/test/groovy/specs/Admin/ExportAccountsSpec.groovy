package specs.Admin

import geb.spock.GebReportingSpec
import pages.Admin.ExportAccountsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import java.text.SimpleDateFormat
import utils.ManageSystemDirectory

@Stepwise
class ExportAccountsSpec extends GebReportingSpec {
    def testKey
    static testCount
    static datePattern = "yyyyMMdd"
    static formattedDate = new SimpleDateFormat(datePattern).format(new Date())
    static projName = "TM-Demo"
    static fileExtension = "xlsx"
    static fileName = "AccountExport-${projName}-${formattedDate}"
    static fullFileName = fileName + "." + fileExtension

    def initializeManageSystemDirectory() {
        new ManageSystemDirectory()
    }

    def setupSpec() {
        testCount = 0
        initializeManageSystemDirectory().createTempDownloadsFolder()
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

    def cleanupSpec() {
        initializeManageSystemDirectory().deleteTempDownloadsFolder()
    }

    def "1. The User Navigates in the Export Accounts Section"() {
        testKey = "TM-XXXX"
        given: 'The User searches in the Menu Page'
            at MenuPage
        when: 'The User Clicks in the Admin > Export Accounts Menu Option'
            adminModule.goToExportAccounts()
        then: 'Export Accounts page should be displayed'
            at ExportAccountsPage
    }

    def "2. The User exports user accounts"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Export Accounts Page'
            at ExportAccountsPage
        when: 'The User selects Staffing type'
            randomSelectStaffing()
        and: 'The user selects User Logins type from dropdown'
            randomSelectUserLogins()
        and: 'The user clicks on export button'
            clickOnExportExcel()
        and: 'The user waits for success file download'
            initializeManageSystemDirectory().waitForDownloadedFile(fileName)
        then: 'The file was successfully downloaded'
            assert initializeManageSystemDirectory().verifyExportedFile(fileName, fullFileName), "Expecting ${fullFileName}"
    }
}