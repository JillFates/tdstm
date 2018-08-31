package specs.Datascripts

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Datascripts.CreateDatascriptPage
import pages.Datascripts.DatascriptDetailsPage
import pages.Datascripts.EditDatascriptPage
import pages.Datascripts.DatascriptsPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage

@Stepwise
class DatascriptEditionSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static E2E = "E2E DS"
    static datascriptName = randStr + E2E + " Name"
    static datascriptDescription = randStr + E2E + " Description"
    static datascriptProvider
    static edit = " edited"
    static datascriptInfo

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
        at DatascriptDetailsPage
        waitFor {dsDetailXIcon.click()}
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user opens the created Datascripts"() {
        given: 'The User is on the Datascripts Page'
            at DatascriptsPage
        and: 'Filters by the datascript Name'
            filterByName datascriptName
        when: 'The user clicks the created Datascript'
            clickOnFirstGridRow()
        then: 'We verify that the Datascript Details pop up is displayed'
            at DatascriptDetailsPage
    }

    def "2. The user closes the Datascript by the X button"() {
        given: 'The user is on the Provider Detail pop up'
            at DatascriptDetailsPage
        when: 'The user clicks the X icon'
            clickOnXButton()
        then:'We are back to the Providers Page'
            at DatascriptsPage
    }

    def "3. The user opens again the datascript and clicks the Edit button"() {
        given: 'The User is on the Providers Page'
            at DatascriptsPage
        when: 'The user clicks on datascript name'
            clickOnFirstGridRow()
        then: 'Datascript details modal is displayed'
            at DatascriptDetailsPage
        when: 'The User clicks the Edit button'
            clickOnEditButton()
        then:'Edit datascript modal is displayed'
            at EditDatascriptPage
    }

    def "4. The user edits the datascript and Saves it"() {
        given: 'The User is on the datascript Edit pop up'
            at EditDatascriptPage
        when: 'The user modifies the info of the Provider'
            datascriptName = datascriptName + edit
            setDsName datascriptName
            datascriptDescription = datascriptDescription + edit
            setDSDescription datascriptDescription
            selectRandomProviderDisplayed()
            datascriptProvider = getSelectedProviderText()
        and: 'Clicks the Save button'
            clickOnSaveButton()
        then:'The page redirects to the datascript page'
            at DatascriptsPage
    }

    def "5. The user searches and opens the edited datascript"() {
        given: 'The User is on the datascript Page'
            at DatascriptsPage
        when: 'Filters by the datascript Name'
            filterByName datascriptName
            datascriptInfo = collectFirstDSInfoDisplayedInGrid()
        then: 'Updated data is properly displayed'
            datascriptInfo.name == datascriptName
            datascriptInfo.description == datascriptDescription
            datascriptInfo.provider == datascriptProvider
    }

    def "6. The user opens again the datascript from edit button and clicks the Edit button"() {
        given: 'The User is on the datascript Page'
            at DatascriptsPage
        when: 'the user clicks on edit button'
            clickOnEditButtonForFirstDS()
        then: 'Datascript details modal is displayed'
            at EditDatascriptPage
    }

    def "7. The user edits the datascript and Saves it again"() {
        given: 'The User is on the datascript Edit pop up'
            at EditDatascriptPage
        when: 'The user modifies the info of the Provider'
            datascriptName = datascriptName + edit
            setDsName datascriptName
            datascriptDescription = datascriptDescription + edit
            setDSDescription datascriptDescription
            selectRandomProviderDisplayed()
            datascriptProvider = getSelectedProviderText()
        and: 'Clicks the Save button'
            clickOnSaveButton()
        then:'The page redirects to the datascript page'
            at DatascriptsPage
    }

    def "8. The user searches and opens the edited datascript"() {
        given: 'The User is on the datascript Page'
            at DatascriptsPage
        when: 'Filters by the datascript Name'
            filterByName datascriptName
            datascriptInfo = collectFirstDSInfoDisplayedInGrid()
        then: 'Updated data is properly displayed'
            datascriptInfo.name == datascriptName
            datascriptInfo.description == datascriptDescription
            datascriptInfo.provider == datascriptProvider
    }
}
