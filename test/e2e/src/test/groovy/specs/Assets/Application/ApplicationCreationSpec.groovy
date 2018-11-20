package specs.Assets.Application

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Assets.AssetViews.AssetCreatePage
import pages.Assets.AssetViews.AssetDetailsPage
import pages.Assets.AssetViews.ViewPage
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
            at ViewPage
    }

    def "2. The User gains access to the Application Creation Window"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User Clicks the "Create App" Button'
            createButton.click()

        then: 'Application Create Pop-up should be displayed'
            at AssetCreatePage
    }

    def "3. A brand new Application is successfully created"() {
        given: 'The User is in the Application Create Pop-up'
            at AssetCreatePage
        when: 'The User completes all the Random Information for Name and Description'
            acModalAppName = appName
            acModalDescription = appDesc
        and: 'The User Searches by SME1'
            acModalSME1Selector.click()
            waitFor { acModalSelectorValues.size() > 2 }
            def selector = acModalSelectorValues[2]
            appSME1 = selector.text()
            waitFor { selector.click() }
        and: 'The User Searches by SME2'
            acModalSME2Selector.click()
            waitFor { acModalSelectorValues.size() > 2 }
            selector = acModalSelectorValues[Math.floorDiv(acModalSelectorValues.size()-1,2)]
            appSME2 = selector.text()
            waitFor { selector.click() }
        and: 'The User Searches by App Owner'
            acModalAppOwnerSelector.click()
            selector = acModalSelectorValues[acModalSelectorValues.size()-1]
            appOwner = selector.text()
            waitFor { acModalSelectorValues.size() > 2 }
            waitFor { selector.click() }
        and: 'The User chooses a Bundle'
            waitFor{ acModalBundleSelector.click()}
            acModalSelectorValues.find(text: appBundle).click()
            acModalBundleSelector.click() // close dropdown
        and: 'The User chooses a proper Status'
            waitFor{ acModalPlanStatusSelector.click()}
            acModalSelectorValues.find(text: appStatus).click()
        and: 'The User clicks the the "Save" Button'
            waitFor { createButton.click() }

        then: ' The User is redirected to the Application Details Page'
            at AssetDetailsPage
        and: 'The User closes the Application Details Page'
            waitFor { closeButton.click() }
        and: 'The User is redirected to the Application List Page'
            at ViewPage
    }

    def "4. The User Filters out by Application Name Name that was recently created"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User Clicks the Application Name Filter Column'
            filterByName(appName)
        and: 'The User searches for it'
            openFirstAssetDisplayed()

        then: 'The Application should be displayed'
            at AssetDetailsPage
    }

    def "5. Validate Application Details"() {
        when: 'The User is on the Application List Page'
            at AssetDetailsPage

        then: 'The User Searches by the AppName, SME1, SME2, AppOwner'
        // TODO some items cannot located due to missing ID's
        waitFor{adModalAppName.text().trim() == appName}
        adModalSME1.text().trim().contains(appSME1) // contains(because user can part of a different company)
        adModalSME2.text().trim().contains(appSME2) // contains(because user can part of a different company)
        adModalAppOwner.text().trim().contains(appOwner) // contains(because user can part of a different company)
    }
}