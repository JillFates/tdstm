package specs.Admin.ImportAccounts

import geb.spock.GebReportingSpec
import pages.Admin.ImportAccounts.ImportAccountsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Ignore

@Ignore
class ImportAccountsSpec extends GebReportingSpec {
    def testKey
    static testCount
    static projName = "TM-Demo"

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

    def "1. The User Navigates in the Import Accounts Section"() {
        testKey = "TM-XXXX"
        given: 'The user searches in the Menu Page'
            at MenuPage
        when: 'The user Clicks in the Admin > Import Accounts Menu Option'
            adminModule.goToImportAccounts()
        then: 'Import Accounts page should be displayed'
            at ImportAccountsPage
        and: 'Import accounts form container to upload file should be displayed'
            waitFor { step1Container.displayed }
    }

    def "2. The User imports accounts file to system"() {
        testKey = "TM-XXXX"
        given: 'The user is on the Import Accounts Page'
            at ImportAccountsPage
        when: 'The user selects the file to be imported'
            selectsFileToImport()
        and: 'The user randomly selects an import option'
            randomSelectImportOption()
        and: 'The user clicks on upload button'
            clickUploadFile()
        then: 'The Step 2 page should be displayed'
            waitFor { step2Title.text() == "Import Accounts - Step 2 > Review Accounts"}
    }
}