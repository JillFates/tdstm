package specs.Projects.Providers

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Projects.Providers.CreateProviderPage
import pages.Projects.Providers.ProvidersPage
import pages.Projects.Providers.ProvidersDetailPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage

@Stepwise
class ProviderDeletionSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static randStr2 = CommonActions.getRandomString() + " "
    static E2E = "E2E Provider to be deleted"
    static provName = randStr + E2E
    static provName2 = randStr2 + E2E
    static deleteMessage = "There are associated Datasources. Deleting this will not delete historical imports. Do you want to proceed?"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        projectsModule.goToProviders()
        at ProvidersPage
        // create first provider to be delete
        waitFor{createBtn.click()}
        at CreateProviderPage
        waitFor {providerNameField.displayed}
        providerNameField = provName
        waitFor {provSaveBtn.isDisplayed()}
        waitFor {provSaveBtn.click()}
        at ProvidersPage
        commonsModule.waitForDialogModalHidden()
        // create second provider to be deleted
        waitFor{createBtn.click()}
        at CreateProviderPage
        waitFor {providerNameField.displayed}
        providerNameField = provName2
        waitFor {provSaveBtn.isDisplayed()}
        waitFor {provSaveBtn.click()}
        at ProvidersPage
        commonsModule.waitForDialogModalHidden()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user opens the first created provider"() {
        given: 'The User is on the Providers Page'
            at ProvidersPage
        and: 'Filters by the provider name'
            filterByName provName
        when: 'The user clicks the created provider'
            clickOnFirstProviderName()
        then: 'We verify that the Provider Details pop up is displayed'
            at ProvidersDetailPage
        and: 'We verify that the details match with the element we created'
            providerName.text() == provName
    }

    def "2. The user clicks on delete button in details modal and gets warning message"() {
        given: 'The user is on the Provider Detail pop up'
            at ProvidersDetailPage
        when: 'The user clicks on delete button'
            clickDeleteButton()
        then:'Delete confirmation modal is displayed'
            commonsModule.waitForPromptModalDisplayed()
        and: 'The user verifies delete message displayed'
            commonsModule.getConfirmationAlertMessageText() == deleteMessage
    }

    def "3. The user clicks on No button and provider is not deleted"() {
        given: 'The User is on the providers delete confirmation modal'
            at ProvidersDetailPage
        and: 'The user clicks on No button'
            commonsModule.clickOnDeleteNoPromptModal()
        and: "The user clicks on X button"
            clickOnXButton()
        when: 'Providers list page is displayed'
            at ProvidersPage
        and: 'Filters by the first provider Name'
            filterByName provName
        then: 'Provider should be displayed'
            getFirstRowProviderGridName() == provName
    }

    def "4. The user opens again first provider created"() {
        given: 'The user clicks the created provider'
            clickOnFirstProviderName()
        when: 'The user clicks Delete button'
            at ProvidersDetailPage
            clickDeleteButton()
        then:'Delete confirmation modal is displayed'
            commonsModule.waitForPromptModalDisplayed()
    }

    def "5. The user deletes the first provider created"() {
        given: 'The user is in confirmation modal'
            commonsModule.waitForPromptModalDisplayed()
        when: 'The User clicks on Yes button in confirmation modal'
            commonsModule.clickOnDeleteYesPromptModal()
        then: 'Confirmation modal is closed'
            commonsModule.waitForPromptModalHidden()
        and: 'Provider modal is also closed'
            commonsModule.waitForDialogModalHidden()
    }

    def "6. The user verifies first provider is deleted"() {
        given: 'Providers list page is displayed'
            at ProvidersPage
        when: 'Filters by the first provider Name'
            filterByName provName
        then: 'No records should be displayed'
            noRecordsRowSize() == 2 // verified Action column and provider column empty
        and: 'No records available message should be displayed'
            getNoRecordsMessageText() == "No records available."
    }

    def "7. The user searches for second created provider"() {
        given: 'The User is on the Providers Page'
            at ProvidersPage
        when: 'Filters by the provider Name'
            filterByName provName2
        then: 'Provider is displayed'
            getFirstRowProviderGridName() == provName2
    }

    def "8. The user clicks on delete button in grid and gets warning message"() {
        given: 'The user is in providers grid and has filtered by provider name'
            at ProvidersPage
        when: 'The user clicks on delete action button in grid'
            clickOnFirstProviderDeleteActionButton()
        then:'Delete confirmation modal is displayed'
            commonsModule.waitForPromptModalDisplayed()
        and: 'The user verifies delete message displayed'
            commonsModule.getConfirmationAlertMessageText() == deleteMessage
    }

    def "9. The user clicks on No and second provider is not deleted"() {
        given: 'The User is on the Providers delete confirmation modal'
            commonsModule.waitForPromptModalDisplayed()
        and: 'The user clicks on No button'
            commonsModule.clickOnDeleteNoPromptModal()
        when: 'Providers list page is displayed'
            at ProvidersPage
        and: 'Filters by the second provider Name'
            filterByName provName2
        then: 'Provider should be displayed'
            getFirstRowProviderGridName() == provName2
    }

    def "10. The user opens again second provider created"() {
        given: 'The user is in providers grid and has filtered by provider name'
            at ProvidersPage
        when: 'The user clicks on delete action button in providers grid'
            clickOnFirstProviderDeleteActionButton()
        then:'Delete confirmation modal is displayed'
            commonsModule.waitForPromptModalDisplayed()
    }

    def "11. The user deletes the provider created"() {
        given: 'The user is in confirmation modal'
            commonsModule.waitForPromptModalDisplayed()
        when: 'The User clicks on Yes button in confirmation modal'
            commonsModule.clickOnDeleteYesPromptModal()
        then: 'Confirmation modal is closed'
            commonsModule.waitForPromptModalHidden()
    }

    def "12. The user verifies second provider is deleted"() {
        given: 'Providers list page is displayed'
            at ProvidersPage
        when: 'Filters by the first provider Name'
            filterByName provName2
        then: 'No records should be displayed'
            noRecordsRowSize() == 2 // verified Action column and provider column empty
        and: 'No records available message should be displayed'
            getNoRecordsMessageText() == "No records available."
    }
}