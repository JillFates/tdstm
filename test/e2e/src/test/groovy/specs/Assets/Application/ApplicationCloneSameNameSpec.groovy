package specs.Assets.Application

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Assets.AssetViews.AssetClonePage
import pages.Assets.AssetViews.AssetCreatePage
import pages.Assets.AssetViews.AssetDetailsPage
import pages.Assets.AssetViews.ViewPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class ApplicationCloneSameNameSpec extends GebReportingSpec {

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
        at ViewPage
        commonsModule.waitForLoader 5
        clickOnCreateButton()
        at AssetCreatePage
        createApplication appDataMap
        at AssetDetailsPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        def sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. User Opens Clone Application Modal"() {
        given: 'The User is on the Application Details Modal'
            at AssetDetailsPage
        when: 'The user clicks on Clone Button'
            clickOnCloneButton()
        then: 'Asset Clone Pop-Up should be displayed'
            at AssetClonePage
            getModalTitle() == "Clone Asset"
        and: 'Asset name did not change'
            getModalInputNameValue() == appName
        and: 'Legend to change the Name should be displayed'
            getValidationModalLegend() == "Change name appropriately"

    }

    def "2. User verifies Confirmation dialog title and legend"() {
        given: 'The User is on the Clone Pop-Up'
            at AssetClonePage
        when: 'The User Clicks the "Clone Button"'
            clickOnCloneButton()
        then: 'Legends to existing Name and a Question should be displayed'
            commonsModule.getConfirmationTitleText() == "Asset already exists"
            commonsModule.getConfirmationAlertMessageText() == "The Asset Name you want to create already exists, do you want to proceed?"
    }

    def "3. User cancels the Asset Clone process and verifies the Asset is not cloned"() {
        given: 'The User is on the Clone Pop-Up'
            at AssetClonePage
        when: 'The User clicks the "Cancel" button'
            commonsModule.clickOnButtonPromptModalByText("Cancel")
        and: 'The user clicks on Close Clone Modal Button'
            closeModal()
        then: 'The User should be redirected to the Application Detail Modal'
            at AssetDetailsPage
        when: 'The user clicks on Close Details Modal Button'
            clickOnCloseButton()
        then: 'The User should be redirected to the Application List Section'
            at ViewPage
        when: 'The User searches by that App Name'
            filterByName appName
        then: 'App Name should be displayed'
            getFirstElementNameText() == appName
            getRowsSize() == 1
    }

    def "4. User clones the App with the Same Name"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User clicks the asset name'
            openFirstAssetDisplayed()
        then: 'The User should be redirected to the Application Details Modal'
            at AssetDetailsPage
        when: 'The User clicks the "Clone" Button'
            clickOnCloneButton()
        then: 'The User is on the Clone Pop-Up'
            at AssetClonePage
        when: 'The User Clicks the Clone Button'
            clickOnCloneButton()
        and: 'The User clicks on Confirm button'
            commonsModule.clickOnButtonPromptModalByText("Confirm")
        then: 'The User should be redirected to the Application Details modal'
            at AssetDetailsPage
            getApplicationName() == appName
    }

    def "5. Validates duplicate application with same name"() {
        given: 'The User is in the Application Details modal'
            at AssetDetailsPage
        and: 'The user closes details modal'
            clickOnCloseButton()
        and: 'The User is in the Application List Page'
            at ViewPage
        when: 'The user filters by app name'
            filterByName appName
        then: 'App Name should be displayed'
            getFirstElementNameText() == appName
        and: 'App is displayed two times'
            getRowsSize() == 2
    }

    def "6. User opens the Clone Application Modal Window By using the Clone Icon"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User clicks the "Clone" Button'
            clickOnFirstAssetCloneActionButton()
        then: 'The User is on the Clone Pop-Up Modal'
            at AssetClonePage
        and: 'The User certifies that the proper App Name should be displayed'
            getModalTitle() == "Clone Asset"
        and: 'Asset name did not change'
            getModalInputNameValue() == appName
        and: 'Legend to change the Name should be displayed'
            getValidationModalLegend() == "Change name appropriately"
    }

    def "7. User clones the App with the Same Name"() {
        given: 'The User is on the Application Clone Modal'
            at AssetClonePage
        when: 'The User Clicks the "Clone Button"'
            clickOnCloneButton()
        and: 'The User clicks on Confirm button'
            commonsModule.clickOnButtonPromptModalByText("Confirm")
        then: 'The User should be redirected to the Application List page'
            at ViewPage
    }

    def "8. Validates triplicate application with same name"() {
        given: 'The User is in the Application List Page'
            at ViewPage
        when: 'The user filters by app name'
            filterByName appName
        then: 'App Name should be displayed'
            getFirstElementNameText() == appName
        and: 'App is displayed three times'
            getRowsSize() == 3
    }
}