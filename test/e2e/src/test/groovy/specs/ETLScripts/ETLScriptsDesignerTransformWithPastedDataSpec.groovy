package specs.ETLScripts

import pages.ETLScripts.*
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class ETLScriptsDesignerTransformWithPastedDataSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static E2E = "E2E DS"
    static datascriptName = randStr + E2E + " Name"
    static datascriptDescription = randStr + E2E + " Description"

    def getContentToPaste() {
        new File("src/test/resources/SampleCsvDataToPasteForDS.txt").text.toString()
    }

    def getETLScript(){
        new File("src/test/resources/DataScriptCode.txt").text.toString()
    }

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()

        at MenuPage
        projectsModule.goToETLScripts()
        at ETLScriptsPage
        createBtn.click()
        at CreateETLScriptsPage
        waitFor{providerDropdown.click()}
        //We select the latest provider that was created
        waitFor{latestProvider.click()}
        waitFor{datascriptDescField.click()}
        datascriptDescField = datascriptDescription
        datascriptNameField = datascriptName
        waitFor {datascriptSaveBtn.isDisplayed()}
        waitFor {datascriptSaveBtn.click()}
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }


    def "1. The user opens ETLScripts Designer modal"() {
        given: 'The User is in ETLScripts details modal'
            at ETLScriptsDetailsPage
        when: 'The user clicks on ETLScripts designer button'
            clickOnDesignerButton()
        then: 'The ETLScripts loads with no problem'
            at ETLScriptsDesignerPage
        and: 'Test, Check Syntax, View Console and Save buttons are disabled'
            getTestButtonVisibility() == "true"
            getCheckSyntaxButtonVisibility() == "true"
            getViewConsoleButtonVisibility() == "true"
            getSaveButtonVisibility() == "true"
    }

    def "2. The user opens Sample Data modal"() {
        given: 'The user is on the ETLScripts designer modal'
            at ETLScriptsDesignerPage
        when: 'The user clicks on Load sample data button'
            clickLoadSampleDataButton()
        then: 'The Sample Data modal is displayed'
            at ETLScriptsDesignerSampleDataPage
    }

    def "3. Certify that data on the Sample Data Preview section is being displayed"() {
        given: 'The user is in Sample Data modal'
            at ETLScriptsDesignerSampleDataPage
        when: 'The user selects source option type Paste content'
            clickOnPasteContent()
        and: 'Selects CSV option from dropdown'
            selectFormatFromDropdown "csv"
        and: 'Pastes the content'
            setContent getContentToPaste()
        and: 'Clicks on Upload button'
            clickOnUploadButton()
        and: 'Clicks on Continue button'
            clickOnContinueButton()
        then: 'The Sample Data rows are displayed'
            at ETLScriptsDesignerPage
            getSampleDataRowsSize() > 1
    }

    def "4. Certify that Data is being displayed on the Transformed Data Preview Section"() {
        given: 'The user is in Sample Data modal'
            at ETLScriptsDesignerPage
        when: 'The user sets ETLScript code'
            setCode getETLScript()
        and: 'Clicks on Save button'
            clickOnSaveButton()
        and: 'Clicks on Check Syntax button'
            clickOnCheckSyntaxButton()
        then: 'No Errors should be displayed'
            getCheckSyntaxSuccessIcon()
        when: 'Clicks on Test button'
            clickOnTestButton()
        then: 'The Transformed Data rows are displayed'
            getTransformedDataRowsSize() > 1
    }
}