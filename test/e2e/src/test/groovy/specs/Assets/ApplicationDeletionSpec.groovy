package specs.Assets

import geb.spock.GebReportingSpec
import pages.Assets.ApplicationDetailsPage
import pages.Assets.ApplicationEditionPage
import pages.Assets.ApplicationListPage
import pages.Assets.ApplicationCreationPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class ApplicationDeletionSpec extends GebReportingSpec {

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static appName = baseName + " " + randStr + " App For E2E Created"
    static appDesc = baseName + " " + randStr + " App Description Created"
    static appBundle = "Buildout"
    static appStatus = "Assigned"
    static appDataMap = [
            appName: appName,
            appDesc: appDesc,
            appBundle: appBundle,
            appStatus: appStatus
    ]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { assetsModule.goToApplications() }
        at ApplicationListPage
        clickOnCreateButton()
        at ApplicationCreationPage
        createApplication appDataMap
        at ApplicationDetailsPage
        closeDetailsModal()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The User Navigates in the Assets Application List Section"() {
        given: 'The User searches in the menu page\''
            at MenuPage
        when: 'The User clicks in the Assets > Applications Menu option'
            assetsModule.goToApplications()

        then: 'Application List should be displayed'
            at ApplicationListPage
    }

    def "2. Filters out by Applications and gets the First occurrence"() {
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User Click in The Filter Name Column'
            waitFor {alNameFilter.click()}
            alNameFilter = appName
        and: 'Adds the AppName in the Filter'
            waitFor{alFirstAppName.text().trim().contains(appName)}
            appName = alFirstAppName.text().trim()
        and: 'Clicks to Filter out'
            waitFor{alFirstAppName.click()}

        then: 'Application should be filtered out'
            at ApplicationDetailsPage
            // TODO The Following item fetched by data-content cannot be located as itself (Label and Value have the same properties)
        and: 'The appName should be shown'
            adModalAppName.text().trim() == appName
    }

    def "3. Using the Edit and Cancel Buttons on dhe Application Modal Windows"() {
        given: 'The User is on the Application Details Page'
            at ApplicationDetailsPage
        when: 'The User clicks the "Edit" Button'
            waitFor {adModalEditBtn.click()}
        then: 'The Option to edit every Option should be displayed'
            at ApplicationEditionPage
            waitFor {aeModalAppName.value() ==  appName}
        when: 'The User clicks the "Cancel" Button'
            waitFor {aeModalCancelBtn.click()}

        then: 'The User should be redirected to the ApplicationListPage'
            at ApplicationListPage
    }

    def "4. Filter Applications on the List by using the App name"() {
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User searches by the App Name'
            waitFor {alNameFilter.click()}
            alNameFilter = appName
            waitFor{alFirstAppName.text().trim() == appName}

        then: 'That App should be shown'
            at ApplicationListPage
    }

    def "5. Opens up The Edit Application Modal Window By using the Edit Icon"() {
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User clicks on the "Edit" Button right on the left'
            waitFor {alFirstAppEdit.click()}

        then: 'The Option to edit every Option should be displayed'
            at ApplicationEditionPage
        and: 'The appName should be displayed'
            aeModalAppName.value() ==  appName
    }

    def "6. Delete the Application already displayed "() {
        given: 'The User is on the Application Edition Section'
            at ApplicationEditionPage
        when: 'The User Deletes the Application'
            withConfirm(true){waitFor {aeModalDeleteBtn.click() }}

        then: 'The user should be redirected to the Application List Section'
            at ApplicationListPage
    }

    def "7. Validating the Application is not on the List"() {
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User searches by the App already deleted'
            waitFor {alNameFilter.click()}
            alNameFilter = appName

        then: 'App should not be visible and Count should be equal to Zero'
            at ApplicationListPage
            waitFor{alGridRows.size() == 0}
    }
}