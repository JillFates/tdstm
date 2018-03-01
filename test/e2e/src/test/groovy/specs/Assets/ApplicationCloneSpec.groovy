package specs.Assets

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.Assets.ApplicationEditionPage
import pages.Assets.AssetClonePage
import pages.Assets.ApplicationCreationPage
import pages.Assets.ApplicationDetailsPage
import pages.Assets.ApplicationListPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class ApplicationCloneSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the app you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "QAE2E"
    static filterpattern = "App For E2E Created"
    static appName
    static appCountBefore
    static appNameCloned = baseName +" "+ randStr + " App For E2E Cloned"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()

        // Note by CN: :'( We'd need to call the ApplicationCreationPage and send the Name we'd need to Edit via a Parameter!
        // NOT to perform the Creation Option in the setupSpec() Method > A New Ticket will be handle separately
        at MenuPage
        waitFor { menuModule.goToApplications() }
        at ApplicationListPage
        waitFor {alNameFilter.click()}
        alNameFilter = filterpattern
        waitFor {alLoadingGrid.displayed}
        waitFor {!alLoadingGrid.displayed}

        if (alGridRows.size() > 0) {
            appName = alFirstAppName.text().trim()
            alNameFilter = appName
            waitFor {alLoadingGrid.displayed}
            waitFor {!alLoadingGrid.displayed}
            appCountBefore = alGridRows.size()
            waitFor{alFirstAppName.click()}
        } else {
            def appNameCreate = baseName + " " + randStr + " App For E2E Created"
            def appDescCreate = baseName + " " + randStr + " App Description Created"
            def appBundleCreate = "Buildout"
            def appStatusCreate = "Assigned"
            appName = appNameCreate
            waitFor { alCreateAppBtn.click() }
            at ApplicationCreationPage
            acModalAppName = appNameCreate
            acModalDescription = appDescCreate
            acModalBundleSelector.click()
            acModalBundleSelector.find("option", text: appBundleCreate).click()
            acModalPlanStatusSelector.click()
            acModalPlanStatusSelector.find("option", text: appStatusCreate).click()
            acModalSaveBtn.click()
            appCountBefore = 1
        }
        at ApplicationDetailsPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        def sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. To Open the Clone Application Modal Window"() {
        testKey = "TM-8494"
        given: 'The User is on the Application Details Page'
            at ApplicationDetailsPage
        when: 'The Clone Button is Displayed'
            waitFor { adModalCloneBtn.displayed }
        and: 'The User searches on It'
            waitFor { adModalCloneBtn.click() }

        then: 'Asset Clone Pop-Up should be displayed'
            at AssetClonePage
            waitFor { asclModalTitle.text().trim() == "Clone " + appName }
    }

    def "2. Close Clone modal window and the Application detail modal Window"() {
        testKey = "TM-8494"
        given: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        when: 'The User clicks the "Close" button'
            waitFor {asclModalCloseBtn.click()}

        then: 'The User should be redirected to the Application Details Section'
            at ApplicationDetailsPage
        and: 'The App should be displayed'
            adModalTitle.text() == appName + " Detail"
        when: 'The User clicks the "Close" button'
            waitFor {adModalCloseBtn.click()}

        then: 'The User should be redirected to the Application List Section'
            at ApplicationListPage
    }

    def "3. Filtering The Application on the List by using the App name"() {
        testKey = "TM-8494"
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User searches by the App Name'
            waitFor {alNameFilter.click()}
            alNameFilter = appName
            waitFor {alLoadingGrid.displayed}
            waitFor {!alLoadingGrid.displayed}
            waitFor{alFirstAppName.text().trim() == appName}

        then: 'That App should be shown'
            at ApplicationListPage
    }

    def "4. Opens the Clone Application Modal Window By using the Clone Icon"() {
        testKey = "TM-8494"
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User clicks the "Clone" Button'
            waitFor {alFirstAppClone.click()}

        then: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        and: 'The User certifies that the proper App Name should be displayed'
            waitFor { asclModalTitle.text().trim() == "Clone " + appName }
    }

    def "5. Verifying the Legend for a Duplicated Cloned Asset name"() {
        testKey = "TM-8494"
        given: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        when: 'The User adds the Same App Name'
            asclModalAssetCloneName.value() == appName

        then: 'Legend to change the Name should be displayed'
            asclModalErrorMsg.text().trim() == "Change name appropriately"
    }

    def "6. Clicking The Clone & Edit Button and verifying the Confirm legend"() {
        testKey = "TM-8494"
        given: 'The User is on the Clone Pop-Up section'
            at AssetClonePage

        when: 'The User Clicks the "Clone and Edit Button"'
            asclModalCloneEditBtn.click()

        then: 'Legends to existing Name and a Question should be displayed'
            asclModalDialogTitle.text().trim() == "Asset already exists"
            asclModalDialogText.text().trim() == "The Asset Name you want to create already exists, do you want to proceed?"
    }

    def "7. Cancels the Asset Clone and verifies the Asset is not cloned"() {
        testKey = "TM-8494"
        given: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        when: 'The User clicks the "Cancel" button'
            waitFor {asclModalDialogCancelbtn.click()}
            waitFor {asclModalCancelBtn.click()}

        then: 'The User should be redirected to the Application List Section'
            at ApplicationListPage
        when: 'The User searches by that App Name'
            waitFor {alNameFilter.click()}
            alNameFilter = appName
            waitFor {alLoadingGrid.displayed}
            waitFor {!alLoadingGrid.displayed}
            waitFor{alFirstAppName.text().trim() == appName}

        then: 'App Name should be displayed'
            at ApplicationListPage
            waitFor{alGridRows.size() == appCountBefore}
    }

    def "8. Opens up Clone Asset again and Clones the App with the Same Name"() {
        testKey = "TM-8494"
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User clicks the "Clone" Button'
            waitFor {alFirstAppClone.click()}

        then: 'The User is on the Clone Pop-Up section'
            at AssetClonePage
        when: 'The User Enters the App Name'
            waitFor { asclModalTitle.text().trim() == "Clone " + appName }
        and: 'The User Clicks the Clone & Edit Button'
            waitFor {asclModalCloneEditBtn.click()}

        then: 'A Dialog asking for permission should be displayed'
            waitFor {asclModalDialog.displayed}

        when: 'The User Confirms It'
            waitFor {asclModalDialogConfirmBtn.click()}

        then: 'The User should be redirected to the Application Edition Section'
            at ApplicationEditionPage
            waitFor {aeModalAppName.value() == appName}
            waitFor {!aeModalUpdateBtn.@disabled}
        when: 'The User clicks the "Update" Button'
            waitFor {aeModalUpdateBtn.click()}

        then: 'The User should be redirected to the Application Details Page'
            at ApplicationDetailsPage
    }

    def "9. Validating Application Details Section"() {
        testKey = "TM-8494"
        when: 'The User is in the Application Details Page'
            at ApplicationDetailsPage

        then: 'App Name and Button to closed should be displayed'
            // TODO Some Application Detail items can't be reached because cannot be identified by itself.
            // TODO Will change this feature after FE code will be changed
            waitFor{adModalAppName[1].text().trim() == appName}
            waitFor{adModalCloseBtn.click()}
    }

    def "10. Checking in the App List the duplicated item"() {
        testKey = "TM-8494"
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User searches by the App Name'
            waitFor {alNameFilter.click()}
            alNameFilter = appName

        then: 'App Name should be duplicated'
            waitFor {alLoadingGrid.displayed}
            waitFor {!alLoadingGrid.displayed}
            waitFor{alGridRows.size() == appCountBefore + 1}
    }
}