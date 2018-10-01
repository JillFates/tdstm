package specs.Assets.Application

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Assets.Application.ApplicationCreationPage
import pages.Assets.Application.ApplicationDetailsPage
import pages.Assets.Application.ApplicationListPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class ApplicationCreationSpec extends GebReportingSpec {

    def testKey
    static testCount

    //Define the names of the Application you will Create and Edit
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static appName = baseName +" "+ randStr + " App For E2E Created"
    static appDesc = baseName +" "+ randStr + " App Description Created"
    static appSME1
    static appSME2
    static appOwner
    static appBundle = "Buildout"
    static appStatus = "Assigned"

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

    def "1. The User Navigates in the Assets Application List Section"() {
        given: 'The User searches in the menu page'
            at MenuPage
        when: 'The User clicks in the Assets > Applications Menu option'
            assetsModule.goToApplications()

        then: 'Application List should be displayed'
            at ApplicationListPage
    }

    def "2. The User gains access to the Application Creation Window"() {
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User Clicks the "Create App" Button'
            alCreateAppBtn.click()

        then: 'Application Create Pop-up should be displayed'
            at ApplicationCreationPage
    }

    def "3. A brand new Application is successfully created"() {
        given: 'The User is in the Application Create Pop-up'
            at ApplicationCreationPage
        when: 'The User completes all the Random Information for Name and Description'
            acModalAppName = appName
            acModalDescription = appDesc
        and: 'The User Searches by SME1'
            acModalSME1Selector.click()
            waitFor { acModalSelectorValues.size() > 2 }
            appSME1 = acModalSelectorValues[2].text()
            waitFor { acModalSelectorValues.find("div", role:"option", text: appSME1).click() }
        and: 'The User Searches by SME2'
            acModalSME2Selector.click()
            waitFor { acModalSelectorValues.size() > 2 }
            appSME2 = acModalSelectorValues[Math.floorDiv(acModalSelectorValues.size()-1,2)].text()
            waitFor { acModalSelectorValues.find("div", role:"option", text: appSME2).click() }
        and: 'The User Searches by App Owner'
            acModalAppOwnerSelector.click()
            appOwner = acModalSelectorValues[acModalSelectorValues.size()-1].text()
            waitFor { acModalSelectorValues.size() > 2 }
            waitFor { acModalSelectorValues.find("div", role:"option", text: appOwner).click() }
        and: 'The User chooses a Bundle'
            acModalBundleSelector = appBundle
        and: 'The User chooses a proper Status'
            acModalPlanStatusSelector = appStatus
        and: 'The User clicks the the "Save" Button'
            waitFor { acModalSaveBtn.click() }

        then: ' The User is redirected to the Application Details Page'
            at ApplicationDetailsPage
        and: 'The User closes the Application Details Page'
            waitFor { adModalCloseBtn.click() }
        and: 'The User is redirected to the Application List Page'
            at ApplicationListPage
    }

    def "4. The User Filters out by Application Name Name that was recently created"() {
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User Clicks the Application Name Filter Column'
            waitFor {alNameFilter.click()}
            alNameFilter = appName
            waitFor{alFirstAppName.text().trim() == appName}
        and: 'The User searches for it'
            waitFor{alFirstAppName.click()}

        then: 'The Application should be displayed'
            at ApplicationDetailsPage
    }

    def "5. Validate Application Details"() {
        when: 'The User is on the Application List Page'
            at ApplicationDetailsPage

        then: 'The User Searches by the AppName, SME1, SME2, AppOwner'
        // TODO some items cannot located due to missing ID's
        waitFor{adModalAppName.text().trim() == appName}
        adModalSME1.text().trim().contains(appSME1) // contains(because user can part of a different company)
        adModalSME2.text().trim().contains(appSME2) // contains(because user can part of a different company)
        adModalAppOwner.text().trim().contains(appOwner) // contains(because user can part of a different company)
    }
}