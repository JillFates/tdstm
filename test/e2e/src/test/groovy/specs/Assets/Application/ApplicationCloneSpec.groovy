package specs.Assets.Application

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Assets.AssetViews.AssetClonePage
import pages.Assets.AssetViews.AssetCreatePage
import pages.Assets.AssetViews.AssetEditPage
import pages.Assets.AssetViews.AssetDetailsPage
import pages.Assets.AssetViews.ViewPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class ApplicationCloneSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the app you will Create and Edit
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
    static appCountBefore

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { assetsModule.goToApplications() }
        at ViewPage
        clickOnCreateButton()
        at AssetCreatePage
        createApplication appDataMap
        appCountBefore = 1
        at AssetDetailsPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        def sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Open the Clone Application Modal Window"() {
        given: 'The User is on the Application Details Page'
            at AssetDetailsPage
        when: 'The user clicks on Clone Button'
            clickOnCloneButton()

        then: 'Asset Clone Pop-Up should be displayed'
            at AssetClonePage
            waitFor { asclModalTitle.text().trim() == "Clone Asset" }
    }

    def "2. Close Clone modal window and the Application detail modal Window"() {
        given: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        when: 'The User clicks the "Close" button'
            waitFor {asclModalCloseBtn.click()}

        then: 'The User should be redirected to the Application Details Section'
            at AssetDetailsPage
        when: 'The User clicks the "Close" button'
            waitFor {closeButton.click()}

        then: 'The User should be redirected to the Application List Section'
            at ViewPage
    }

    def "3. Filtering The Application on the List by using the App name"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User searches by the App Name'
            filterByName(appName)

        then: 'That App should be shown'
            getFirstElementNameText() == appName
    }

    def "4. Opens the Clone Application Modal Window By using the Clone Icon"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User clicks the "Clone" Button'
            clickOnFirstAssetCloneActionButton()

        then: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        and: 'The User certifies that the proper App Name should be displayed'
            waitFor { asclModalTitle.text().trim() == "Clone Asset" }
    }

    def "5. Verifying the Legend for a Duplicated Cloned Asset name"() {
        given: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        when: 'The User adds the Same App Name'
            asclModalAssetCloneName.value() == appName

        then: 'Legend to change the Name should be displayed'
            asclModalErrorMsg.text().trim() == "Change name appropriately"
    }

    def "6. Clicking The Clone & Edit Button and verifying the Confirm legend"() {
        given: 'The User is on the Clone Pop-Up section'
            at AssetClonePage

        when: 'The User Clicks the "Clone and Edit Button"'
            asclModalCloneEditBtn.click()

        then: 'Legends to existing Name and a Question should be displayed'
            commonsModule.getConfirmationTitleText() == "Asset already exists"
            commonsModule.getConfirmationAlertMessageText() == "The Asset Name you want to create already exists, do you want to proceed?"
    }

    def "7. Cancels the Asset Clone and verifies the Asset is not cloned"() {
        given: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        when: 'The User clicks the "Cancel" button'
            commonsModule.clickOnButtonPromptModalByText("Cancel")
            waitFor {asclModalCancelBtn.click()}

        then: 'The User should be redirected to the Application List Section'
            at ViewPage
        when: 'The User searches by that App Name'
            filterByName(appName)

        then: 'App Name should be displayed'
            getFirstElementNameText() == appName
            getRowsSize() == appCountBefore
    }

    def "8. Opens up Clone Asset again and Clones the App with the Same Name"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User clicks the "Clone" Button'
            clickOnFirstAssetCloneActionButton()

        then: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        when: 'The User Enters the App Name'
            waitFor { asclModalTitle.text().trim() == "Clone Asset" }
        and: 'The User Clicks the Clone & Edit Button'
            waitFor {asclModalCloneEditBtn.click()}

        then: 'A Dialog asking for permission should be displayed'
            commonsModule.waitForPromptModalDisplayed()

        when: 'The User Confirms It'
            commonsModule.clickOnButtonPromptModalByText("Confirm")

        then: 'The User should be redirected to the Application Edition Section'
            at AssetEditPage
            waitFor {aeModalAppName.value() == appName}
            waitFor {!saveButton.@disabled}
        when: 'The User clicks the "Update" Button'
            waitFor {saveButton.click()}

        then: 'The User should be redirected to the Application Details Page'
            at AssetDetailsPage
    }

    def "9. Validating Application Details Section"() {
        when: 'The User is in the Application Details Page'
            at AssetDetailsPage

        then: 'App Name and Button to closed should be displayed'
            // TODO Some Application Detail items can't be reached because cannot be identified by itself.
            // TODO Will change this feature after FE code will be changed
            waitFor{adModalAppName.text().trim() == appName}
            waitFor{closeButton.click()}
    }

    def "10. Checking in the App List the duplicated item"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User searches by the App Name'
            filterByName(appName)

        then: 'App Name should be duplicated'
            getRowsSize() == appCountBefore + 1
    }
}