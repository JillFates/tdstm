package specs.Projects.ETLScripts

import pages.Projects.ETLScripts.*
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

// import geb.driver.CachingDriverFactory

@Stepwise
class ETLScriptsCreationSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static E2E = "E2E DS"
    static datascriptName = randStr + E2E + " Name"
    static datascriptDescription = randStr + E2E + " Description"



    def setupSpec() {
        // CachingDriverFactory.clearCacheAndQuitDriver()
        
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


    def "1. The user navigates to the ETLScripts Section"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The user goes to the ETLScripts page'
            waitFor{ projectsModule.goToETLScripts()}

        then: 'The ETLScripts Page loads with no problem'
            at ETLScriptsPage
    }

    def "2. Open the Create ETLScripts pop up and close it"() {
        given: 'The user is on the ETLScripts landing page'
            at ETLScriptsPage
        when: 'The user clicks the Create ETLScripts Button'
            createBtn.click()
            at CreateETLScriptsPage

        then: 'The pop up loads with no problem'
            waitFor{datascriptXIcon.click()}
        and: 'And it is closed again'
            !datascriptXIcon.displayed
    }

    def "3. Create a ETLScripts"() {
        given: 'The user is on the ETLScripts landing page'
            at ETLScriptsPage
        and: 'Opens the Create ETLScripts pop up'
            waitFor{createBtn.click()}
            at CreateETLScriptsPage
        when: 'The user fills the necessary data to create a ETLScripts'
            datascriptDescField = datascriptDescription
            datascriptNameField = datascriptName
            waitFor{providerDropdown.click()}
            //We select the latest provider that was created
            waitFor{latestProvider.click()}
            waitFor{datascriptDescField.click()}
            waitFor {datascriptSaveBtn.isDisplayed()}
            waitFor {datascriptSaveBtn.click()}

        then: 'The ETLScripts Create popup is closed'
            at ETLScriptsPage
    }

    def "4. Search the ETLScripts"(){
        given: 'The user is on the ETLScripts Detail pop up page after a ETLScripts was created'
            at ETLScriptsPage
        when: 'The user clicks on Filter button'
            clickOnFilterButton()
        and: 'The user filters by the DS name'
            filterByName(datascriptName)
            waitFor{firstDS.text().contains(datascriptName)}
        then: 'The DS is displayed and we verify that it is the same we just created'
            firstDSName.text() == datascriptName
    }

}
