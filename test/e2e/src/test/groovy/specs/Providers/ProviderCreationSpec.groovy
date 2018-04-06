package specs.Providers

import pages.Providers.CreateProviderPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import jodd.util.RandomString
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import pages.Providers.ProvidersPage


@Stepwise
class ProviderCreationSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr =  RandomString.getInstance().randomAlphaNumeric(4) + " "
    static E2E = "E2E Provider"
    static provName = randStr + E2E + " Name"
    static provDescription = randStr + E2E + " Description"
    static provComment = randStr + E2E + " Comment"


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
        testKey = "TM-XXXX"
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The user goes to the Providers page'
            menuModule.goToProviders()

        then: 'The Providers Page loads with no problem'
            at ProvidersPage
    }

    def "2. Open the Create Providers Page"() {
        testKey = "TM-XXXX"
        given: 'The user is on the Providers landing page'
            at ProvidersPage
        when: 'The user clicks the Create Providers Page'
            createBtn.click()

        then: 'The Create Providers pop up loads with no problem'
            at CreateProviderPage
    }

    def "3. Create a Provider"() {
        testKey = "TM-XXXX"
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

    def "4. Search the Provider"(){
        testKey = "TM-XXXX"
        given: 'The user is on the Providers page after a Provider was created'
            at ProvidersPage
        when: 'The user clicks the Name filter'
            waitFor {nameFilter.click()}
        and: 'Filters by the provider Name'
            nameFilter = provName
        //This is to make sure that if 2 different providers start with the same characters,
        //that we get exactly that one we just created.
            waitFor {nameFilter == provName}

        then: 'The provider is displayed and we verify that it is the same we just created'
            firstProvider.text() == provName
    }



}
