package specs.Projects.Providers

import pages.Projects.Providers.CreateProviderPage
import pages.Projects.Providers.ProvidersDetailPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import pages.Projects.Providers.ProvidersPage


@Stepwise
class ProviderCreationSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static E2E = "E2E Provider"
    static provName = randStr + E2E + " Name"
    static provDescription = randStr + E2E + " Description"
    static provComment = randStr + E2E + " Comment"
    static provDate = ""


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

    def "1. The user navigates to the Providers Section"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The user goes to the Providers page'
            projectsModule.goToProviders()

        then: 'The Providers Page loads with no problem'
            at ProvidersPage
    }

    def "2. Open the Create Providers Page"() {
        given: 'The user is on the Providers landing page'
            at ProvidersPage
        when: 'The user clicks the Create Providers Page'
            createBtn.click()

        then: 'The Create Providers pop up loads with no problem'
            at CreateProviderPage
    }

    def "3. Create a Provider"() {
        given: 'The user is on the Providers landing page'
            at CreateProviderPage
        when: 'The user fills the necessary data to create a Provider'
            providerNameField = provName
            providerDescField = provDescription
            providerCommentField = provComment
            waitFor {provSaveBtn.isDisplayed()}
            waitFor {provSaveBtn.click()}

        then: 'The Providers page is loaded with no problem'
            at ProvidersPage
    }

    def "4. Search the Provider by name"(){
        given: 'The user is on the Providers page after a Provider was created'
            at ProvidersPage
        when: 'The user clicks the Name filter'
            waitFor {nameFilter.click()}
        and: 'Filters by the provider Name'
            nameFilter = provName
        //This is to make sure that if 2 different providers start with the same characters,
        //we get exactly the one we just created.
            waitFor {nameFilter == provName}
            provDate = firstProviderDate.text()

        then: 'The provider is displayed and we verify that it is the same we just created'
            firstProviderName.text() == provName
    }

    def "5. Filter providers by date and description"(){
        given: 'The user is still on the Providers page'
            at ProvidersPage
        when: 'The user clears the Name filter'
            nameFilter = ""
            waitFor {nameFilter == ""}
        and: 'The user filters by the date of the last created provider'
            //This is necessary to handle Kendo date format
            provDate = provDate.replace("/",".")
            dateFilter.value(provDate)
        and: 'The user clicks the Description filter'
            waitFor{descriptionFilter.click()}
        and: 'Filters by the Description'
            descriptionFilter = provDescription
            waitFor {descriptionFilter == provDescription}

        then: 'The provider is displayed and we verify that it is the same we just created'
            firstProviderDesc.text() == provDescription 
    }

    def "6. Refresh the table and verify the element we just created is still present"(){
        given: 'The user is on the Provider Page with the latest created provider filtered'
            at ProvidersPage
        when: 'The user clicks the Refresh Icon'
            waitFor {refreshBtn.click()}

        then: 'We verify that the latest provider is still displayed'
            waitFor{firstProviderDesc.text() == provDescription}
    }

    def "7. Verify the opened provider is the one we created"(){
        given: 'The user is on the Provider Page with the latest created provider filtered'
            at ProvidersPage
        when: 'The user clicks the first element'
            waitFor {firstProviderDesc.click()}

        then: 'We verify that the Provider Details pop up is displayed'
            at ProvidersDetailPage
        and: 'We verify that the details match with the element we created'
            providerDesc.text() == provDescription
            providerName.text() == provName
    }

    def "8. We close the pop up and verify we land on the Providers Page"(){
        given: 'The user is on the Provider Detail pop up'
            at ProvidersDetailPage
        when: 'The user clicks the X icon'
            waitFor {closeXIcon.click()}

        then:'We are back to the Providers Page'
            at ProvidersPage
    }

    def "9. We click the Asc/Desc option for any column and verify the provider is still displayed"(){
        given: 'The user is on the Providers Page'
            at ProvidersPage
        when: 'The user clicks asc/desc option for the name column'
            waitFor {nameColumnHeader.click()}

        then: 'The provider is displayed and we verify that it is the same we just created'
            firstProviderName.text() == provName
    }

}
