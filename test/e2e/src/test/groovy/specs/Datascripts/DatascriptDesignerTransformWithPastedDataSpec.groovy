package specs.Datascripts

import pages.Datascripts.CreateDatascriptPage
import pages.Datascripts.DatascriptDetailsPage
import pages.Datascripts.DatascriptsPage
import pages.Datascripts.DatascriptDesignerPage
import pages.Datascripts.DatascriptDesignerSampleDataPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class DatascriptDesignerTransformWithPastedDataSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static E2E = "E2E DS"
    static datascriptName = randStr + E2E + " Name"
    static datascriptDescription = randStr + E2E + " Description"

    def getContentToPaste() {
        new File("src/test/resources/SampleCsvDataToPasteForDS.txt").text.toString()
    }

    def getDataScript(){
        new File("src/test/resources/DataScriptCode.txt").text.toString()
    }

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()

        at MenuPage
        projectsModule.goToDatascripts()
        at DatascriptsPage
        createBtn.click()
        at CreateDatascriptPage
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


    def "1. The user opens Datascript Designer modal"() {
        given: 'The User is in datascript details modal'
            at DatascriptDetailsPage
        when: 'The user clicks on datascript designer button'
            clickOnDesignerButton()
        then: 'The Datascripts Page loads with no problem'
            at DatascriptDesignerPage
        and: 'Test, Check Syntax, View Console and Save buttons are disabled'
            getTestButtonVisibility() == "true"
            getCheckSyntaxButtonVisibility() == "true"
            getViewConsoleButtonVisibility() == "true"
            getSaveButtonVisibility() == "true"
    }

    def "2. The user opens Sample Data modal"() {
        given: 'The user is on the Datascript designer modal'
            at DatascriptDesignerPage
        when: 'The user clicks on Load sample data button'
            clickLoadSampleDataButton()
        then: 'The Sample Data modal is displayed'
            at DatascriptDesignerSampleDataPage
    }

    def "3. Certify that data on the Sample Data Preview section is being displayed"() {
        given: 'The user is in Sample Data modal'
            at DatascriptDesignerSampleDataPage
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
            at DatascriptDesignerPage
            getSampleDataRowsSize() > 1
    }

    def "4. Certify that Data is being displayed on the Transformed Data Preview Section"() {
        given: 'The user is in Sample Data modal'
            at DatascriptDesignerPage
        when: 'The user sets datascript code'
            setCode getDataScript()
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