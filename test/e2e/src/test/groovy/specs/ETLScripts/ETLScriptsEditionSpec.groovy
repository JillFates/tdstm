package specs.ETLScripts

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.ETLScripts.*
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage

@Stepwise
class ETLScriptsEditionSpec extends GebReportingSpec{

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
        at ETLScriptsDetailsPage
        waitFor {dsDetailXIcon.click()}
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user opens the created ETLScripts"() {
        given: 'The User is on the ETLScripts Page'
            at ETLScriptsPage
        and: 'Filters by the ETLScripts Name'
            filterByName datascriptName
        when: 'The user clicks the created ETLScripts'
            clickOnFirstGridRow()
        then: 'We verify that the ETLScripts Details pop up is displayed'
            at ETLScriptsDetailsPage
    }

    def "2. The user closes the ETLScripts by the X button"() {
        given: 'The user is on the Provider Detail pop up'
            at ETLScriptsDetailsPage
        when: 'The user clicks the X icon'
            clickOnXButton()
        then:'We are back to the Providers Page'
            at ETLScriptsPage
    }

    def "3. The user opens again the ETLScripts and clicks the Edit button"() {
        given: 'The User is on the Providers Page'
            at ETLScriptsPage
        when: 'The user clicks on ETLScripts name'
            clickOnFirstGridRow()
        then: 'ETLScripts details modal is displayed'
            at ETLScriptsDetailsPage
        when: 'The User clicks the Edit button'
            clickOnEditButton()
        then:'Edit ETLScripts modal is displayed'
            at EditETLScriptsPage
    }

    def "4. The user edits the ETLScripts and Saves it"() {
        given: 'The User is on the ETLScripts Edit pop up'
            at EditETLScriptsPage
        when: 'The user modifies the info of the Provider'
            datascriptName = datascriptName + edit
            setDsName datascriptName
            datascriptDescription = datascriptDescription + edit
            setDSDescription datascriptDescription
            selectRandomProviderDisplayed()
            datascriptProvider = getSelectedProviderText()
        and: 'Clicks the Save button'
            clickOnSaveButton()
        then:'The page redirects to the ETLScripts page'
            at ETLScriptsPage
    }

    def "5. The user searches and opens the edited ETLScripts"() {
        given: 'The User is on the ETLScripts Page'
            at ETLScriptsPage
        when: 'Filters by the ETLScripts Name'
            filterByName datascriptName
            datascriptInfo = collectFirstDSInfoDisplayedInGrid()
        then: 'Updated data is properly displayed'
            datascriptInfo.name == datascriptName
            datascriptInfo.description == datascriptDescription
            datascriptInfo.provider == datascriptProvider
    }

    def "6. The user opens again the ETLScripts from edit button and clicks the Edit button"() {
        given: 'The User is on the ETLScripts Page'
            at ETLScriptsPage
        when: 'the user clicks on edit button'
            clickOnEditButtonForFirstDS()
        then: 'ETLScripts details modal is displayed'
            at EditETLScriptsPage
    }

    def "7. The user edits the ETLScripts and Saves it again"() {
        given: 'The User is on the ETLScripts Edit pop up'
            at EditETLScriptsPage
        when: 'The user modifies the info of the Provider'
            datascriptName = datascriptName + edit
            setDsName datascriptName
            datascriptDescription = datascriptDescription + edit
            setDSDescription datascriptDescription
            selectRandomProviderDisplayed()
            datascriptProvider = getSelectedProviderText()
        and: 'Clicks the Save button'
            clickOnSaveButton()
        then:'The page redirects to the ETLScripts page'
            at ETLScriptsPage
    }

    def "8. The user searches and opens the edited ETLScripts"() {
        given: 'The User is on the ETLScripts Page'
            at ETLScriptsPage
        when: 'Filters by the ETLScripts Name'
            filterByName datascriptName
            datascriptInfo = collectFirstDSInfoDisplayedInGrid()
        then: 'Updated data is properly displayed'
            datascriptInfo.name == datascriptName
            datascriptInfo.description == datascriptDescription
            datascriptInfo.provider == datascriptProvider
    }
}
