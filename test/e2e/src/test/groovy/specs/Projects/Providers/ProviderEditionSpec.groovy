package specs.Projects.Providers

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Projects.Providers.CreateProviderPage
import pages.Projects.Providers.ProvidersPage
import pages.Projects.Providers.ProvidersDetailPage
import pages.Projects.Providers.ProvidersEditionPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage

@Stepwise
class ProviderEditionSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static E2E = "E2E Provider"
    static provName = randStr + E2E + " Name"
    static provDescription = randStr + E2E + " Description"
    static provComment = randStr + E2E + " Comment"
    static provDate = ""
    static edit = " edited"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        projectsModule.goToProviders()
        at ProvidersPage
        waitFor{createBtn.click()}
        at CreateProviderPage
        waitFor {providerDescField.click()}
        providerDescField = provDescription
        waitFor {providerCommentField.click()}
        providerCommentField = provComment
        waitFor {providerNameField.click()}
        providerNameField = provName
        waitFor {provSaveBtn.isDisplayed()}
        waitFor {provSaveBtn.click()}
        at ProvidersPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user opens the created Provider"() {
        given: 'The User is on the Providers Page'
            at ProvidersPage
        and: 'The user clicks the Name filter'
            waitFor {nameFilter.click()}
        and: 'Filters by the provider Name'
            nameFilter = provName
            //This is to make sure that if 2 different providers start with the same characters,
            //we get exactly the one we just created.
            waitFor {nameFilter == provName}
        when: 'The user clicks the created Provider'
            waitFor {firstProviderDesc.click()}

        then: 'We verify that the Provider Details pop up is displayed'
            at ProvidersDetailPage
            and: 'We verify that the details match with the element we created'
            providerName.text() == provName
    }

    def "2. The user closes the Provider by the X button"() {
        given: 'The user is on the Provider Detail pop up'
            at ProvidersDetailPage
        when: 'The user clicks the X icon'
            waitFor {closeXIcon.click()}

        then:'We are back to the Providers Page'
            at ProvidersPage
    }

    def "3. The user opens again the provider and clicks the Edit button"() {
        given: 'The User is on the Providers Page'
            at ProvidersPage
        and: 'The user clicks the Name filter'
            waitFor {nameFilter.click()}
        and: 'Filters by the provider Name'
            nameFilter = provName
            //This is to make sure that if 2 different providers start with the same characters,
            //we get exactly the one we just created.
            waitFor {nameFilter == provName}
        and: 'The user clicks the created Provider'
            waitFor {firstProviderDesc.click()}
            at ProvidersDetailPage
        and: 'We verify that the details match with the element we created'
            providerName.text() == provName
        when: 'The User clicks the Edit button'
            waitFor {editBtn.click()}

        then:'The Provider can be edited'
            editBtn.text()=="Save"
    }

    def "4. The user edits the Provider and Saves it"() {
        given: 'The User is on the Provider Edit pop up'
            at ProvidersEditionPage
        when: 'The user modifies the info of the Provider'
            waitFor {provNameTxtField.click()}
            provNameTxtField = provName + edit
            waitFor {provDescTxtField.click()}
            provDescTxtField = provDescription + edit
            waitFor {provCommentTxtField.click()}
            provCommentTxtField = provComment + edit
        and: 'Clicks the Save button'
            waitFor {saveBtn.click()}

        then:'The page redirects to the Provider page'
            at ProvidersPage
    }

    def "5. The user searches and opens the edited Provider"() {
        given: 'The User is on the Providers Page'
            at ProvidersPage
            and: 'The user clicks the Name filter'
            waitFor {nameFilter.click()}
        and: 'Filters by the provider Name'
            //We clear the filter first
            nameFilter = ""
            nameFilter = provName + edit
            //This is to make sure that if 2 different providers start with the same characters,
            //we get exactly the one we just created.
            waitFor {nameFilter == provName + edit}
        when: 'The user clicks the edit button from the first Provider'
            waitFor {firstProviderEditPencilBtn.click()}

        then: 'We verify that the Provider Details pop up is displayed'
            at ProvidersEditionPage
        and: 'We verify that the details match with the element we created'
            provNameTxtField.value() == provName + edit
    }

}
