package specs.Projects.ETLScripts

import pages.Projects.ETLScripts.*
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class ETLScriptsCreationSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static E2E = "E2E DS"
    static datascriptName = randStr + E2E + " Name"
    static datascriptDescription = randStr + E2E + " Description"



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


    def "1. The user navigates to the ETLScripts Section"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The user goes to the ETLScripts page'
            projectsModule.goToETLScripts()

        then: 'The ETLScripts Page loads with no problem'
            at ETLScriptsPage
    }

    def "2. Open the Create ETLScripts pop up and close it"() {
        given: 'The user is on the ETLScripts landing page'
            at ETLScriptsPage
        when: 'The user clicks the Create ETLScripts Button'
            createBtn.click()

        then: 'The pop up loads with no problem and it is closed again'
            at CreateETLScriptsPage
            waitFor{datascriptXIcon.click()}
            commonsModule.waitForDialogModalHidden()
    }

    def "3. Create a ETLScripts"() {
        given: 'The user is on the ETLScripts landing page'
            at ETLScriptsPage
        and: 'Opens the Create ETLScripts pop up'
            waitFor{createBtn.click()}
            at CreateETLScriptsPage
        when: 'The user fills the necessary data to create a ETLScripts'
            waitFor{providerDropdown.click()}
            //We select the latest provider that was created
            waitFor{latestProvider.click()}
            waitFor{datascriptDescField.click()}
            datascriptDescField = datascriptDescription
            datascriptNameField = datascriptName
            waitFor {datascriptSaveBtn.isDisplayed()}
            waitFor {datascriptSaveBtn.click()}
            commonsModule.waitForDialogModalHidden()
        then: 'The ETLScripts Detail page is displayed'
            at ETLScriptsDetailsPage
    }

    def "4. Close the detail pop up and search the ETLScripts"(){
        given: 'The user is on the ETLScripts Detail pop up page after a ETLScripts was created'
            at ETLScriptsDetailsPage
        when: 'The user closes the details pop up'
            waitFor{dsDetailXIcon.click()}
        and: 'The ETLScripts Page is displayed'
            at ETLScriptsPage
        and: 'The user clicks the Name filter'
            waitFor {nameFilter.click()}
        and: 'Filters by the DS Name'
            nameFilter = datascriptName
        //This is to make sure that if 2 different DS start with the same characters,
        //that we get exactly the one we just created.
            waitFor {nameFilter == datascriptName}

        then: 'The DS is displayed and we verify that it is the same we just created'
            firstDS.text() == datascriptName
    }

}
