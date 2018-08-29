package specs.Datascripts

import pages.Datascripts.CreateDatascriptPage
import pages.Datascripts.DatascriptDetailsPage
import pages.Datascripts.DatascriptsPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class DatascriptCreationSpec extends GebReportingSpec{

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


    def "1. The user navigates to the Datascripts Section"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The user goes to the Datascripts page'
            projectsModule.goToDatascripts()

        then: 'The Datascripts Page loads with no problem'
            at DatascriptsPage
    }

    def "2. Open the Create Datascripts pop up and close it"() {
        given: 'The user is on the Datascript landing page'
            at DatascriptsPage
        when: 'The user clicks the Create Datascripts Button'
            createBtn.click()

        then: 'The pop up loads with no problem and it is closed again'
            at CreateDatascriptPage
            waitFor{datascriptXIcon.click()}
            commonsModule.waitForDialogModalHidden()
    }

    def "3. Create a Datascript"() {
        given: 'The user is on the Datascripts landing page'
            at DatascriptsPage
        and: 'Opens the Create Datascript pop up'
            waitFor{createBtn.click()}
            at CreateDatascriptPage
        when: 'The user fills the necessary data to create a Datascript'
            waitFor{providerDropdown.click()}
            //We select the latest provider that was created
            waitFor{latestProvider.click()}
            waitFor{datascriptDescField.click()}
            datascriptDescField = datascriptDescription
            datascriptNameField = datascriptName
            waitFor {datascriptSaveBtn.isDisplayed()}
            waitFor {datascriptSaveBtn.click()}

        then: 'The Datascript Detail page is displayed'
            at DatascriptDetailsPage
    }

    def "4. Close the detail pop up and search the Datascript"(){
        given: 'The user is on the Datascript Detail pop up page after a Datascript was created'
            at DatascriptDetailsPage
        when: 'The user closes the details pop up'
            waitFor{dsDetailXIcon.click()}
        and: 'The Datascript Page is displayed'
            at DatascriptsPage
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
