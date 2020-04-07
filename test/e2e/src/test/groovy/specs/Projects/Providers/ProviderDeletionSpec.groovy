package specs.Projects.Providers

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Projects.Providers.CreateProviderPage
import pages.Projects.Providers.ProvidersPage
import pages.Projects.Providers.ProvidersDetailPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage

// import geb.driver.CachingDriverFactory

@Stepwise
class ProviderDeletionSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static randStr2 = CommonActions.getRandomString() + " "
    static E2E = "E2E Provider to be deleted"
    static provName = randStr + E2E
    static provName2 = randStr2 + E2E

    def setupSpec() {
        // CachingDriverFactory.clearCacheAndQuitDriver()
        
        testCount = 0
        to LoginPage
        login()
        sleep(1500)
        at MenuPage
        projectsModule.goToProviders()
        at ProvidersPage
        // create first provider to be deleted
        waitFor{createBtn.click()}
        at CreateProviderPage
        waitFor {providerNameField.displayed}
        providerNameField = provName
        waitFor {provSaveBtn.isDisplayed()}
        waitFor {provSaveBtn.click()}
        at ProvidersPage
        // create second provider to be deleted
        waitFor{createBtn.click()}
        sleep(1000)
        at CreateProviderPage
        waitFor {providerNameField.displayed}
        providerNameField = provName2
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

    def "2. The user clicks the delete button on the details modal and the modal is displayed"() {
        given: 'The user is on the Provider Detail pop up'
            at ProvidersDetailPage
        when: 'The user clicks on delete button'
            clickDeleteButton()
        then:'The Confirmation Required modal is displayed'
            waitFor{confirmation.displayed}
    }

    def "3. The user clicks the No button and provider is not deleted"() {
        given: 'The User is on the providers delete confirmation modal'
            at ProvidersDetailPage
        when: 'The user clicks the No button'
            commonsModule.clickOnButtonDialogModalByText("No")
        then: 'The Details modal is still displayed'
            at ProvidersDetailPage
    }

    def "4. The user deletes the first provider"() {
        given: 'The user clicks the Delete button'
            clickDeleteButton()
        when: 'The user clicks Yes on the confirmation modal'
            waitFor{confirmation.displayed}
            commonsModule.clickOnButtonDialogModalByText("Yes")
        then:'The Providers page is displayed'
            at ProvidersPage
    }

    def "5. The user verifies first provider is deleted"() {
        given: 'Providers list page is displayed'
            at ProvidersPage
        when: 'Filters by the first provider Name'
            filterByName provName
        then: 'No records should be displayed'
            noRecordsRowSize() == 2 // verified Action column and provider column empty
        and: 'No records available message should be displayed'
            getNoRecordsMessageText() == "No records available."
    }

    def "6. The user searches for the 2nd created provider"() {
        given: 'The User is on the Providers Page'
            at ProvidersPage
        when: 'The user filters by the provider Name'
            filterByName provName2
        then: 'Provider is displayed'
            getFirstRowProviderGridName() == provName2
    }

    def "7. The user tries to delete the 2nd provider from the Action column"() {
        given: 'The user is in providers grid and has filtered by provider name'
            at ProvidersPage
        when: 'The user clicks on delete action button in grid'
            clickOnFirstProviderDeleteActionButton()
        then:'Delete confirmation modal is displayed'
            waitFor{confirmation.displayed}
    }

    def "8. The user clicks on No and the 2nd provider is not deleted"() {
        given: 'The User is on the Providers delete confirmation modal'
            waitFor{confirmation.displayed}
        and: 'The user clicks on No button'
            commonsModule.clickOnButtonDialogModalByText("No")
        when: 'Providers list page is displayed'
            at ProvidersPage
        and: 'Filters by the second provider Name'
            filterByName provName2
        then: 'Provider should be displayed'
            getFirstRowProviderGridName() == provName2
    }

    def "9. The user opens again the 2nd provider created"() {
        given: 'The user is in providers grid and has filtered by provider name'
            at ProvidersPage
        when: 'The user clicks on delete action button in providers grid'
            clickOnFirstProviderDeleteActionButton()
        then:'Delete confirmation modal is displayed'
            confirmation.displayed
    }

    def "10. The user deletes the 2nd provider created"() {
        given: 'The user is in confirmation modal'
            confirmation.displayed
        when: 'The User clicks on Yes button in confirmation modal'
            commonsModule.clickOnButtonDialogModalByText("Yes")
        then: 'Confirmation modal is closed'
            !confirmation.displayed
    }

    def "11. The user verifies the 2nd provider is deleted"() {
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
