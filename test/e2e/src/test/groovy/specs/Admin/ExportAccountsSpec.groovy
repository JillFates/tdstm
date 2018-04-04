package specs.Admin

import geb.spock.GebReportingSpec
import pages.Admin.ExportAccountsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import java.text.SimpleDateFormat

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

    def setupSpec() {
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
        and: 'The User makes sure Downloads folder does not contain previous file version'
            cleanUserHomeDownloadsFolder()
        when: 'The User selects Staffing type'
            randomSelectStaffing()
        and: 'The user selects User Logins type from dropdown'
            randomSelectUserLogins()
        and: 'The user clicks on export button'
            clickOnExportExcel()
        and: 'The user waits for success file download'
            waitForDownloadedFile(fileName)
        then: 'The file was successfully downloaded'
            assert verifyExportedFile(fileName, fullFileName), "Expecting ${fullFileName}"
    }
}