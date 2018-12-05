package specs.Assets.Application

import geb.spock.GebReportingSpec
import pages.Assets.AssetViews.AssetCreatePage
import pages.Assets.AssetViews.AssetEditPage
import pages.Assets.AssetViews.AssetDetailsPage
import pages.Assets.AssetViews.ViewPage
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
        at ViewPage
        clickOnCreateButton()
        at AssetCreatePage
        createApplication appDataMap
        at AssetDetailsPage
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
            at ViewPage
    }

    def "2. Filters out by Applications and gets the First occurrence"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User searches by the App Name already created'
            filterByName(appName)
        and: 'The User clicks on that Application'
            openFirstAssetDisplayed()

        then: 'Application should be filtered out'
            at AssetDetailsPage
            // TODO The Following item fetched by data-content cannot be located as itself (Label and Value have the same properties)
        and: 'The appName should be shown'
            adModalAppName.text().trim() == appName
    }

    def "3. Using the Edit and Cancel Buttons on dhe Application Modal Windows"() {
        given: 'The User is on the Application Details Page'
            at AssetDetailsPage
        when: 'The User clicks the "Edit" Button'
            waitFor {editButton.click()}
        then: 'The Option to edit every Option should be displayed'
            at AssetEditPage
            waitFor {aeModalAppName.value() ==  appName}
        when: 'The User clicks the "Cancel" Button'
            commonsModule.goToElement(cancelButton)
            waitFor {cancelButton.click()}

        then: 'The User should be redirected to the ApplicationListPage'
            at ViewPage
    }

    def "4. Filter Applications on the List by using the App name"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User searches by the App Name'
            filterByName(appName)
            waitFor{assetNames[0].text().trim() == appName}

        then: 'That App should be shown'
            at ViewPage
    }

    def "5. Opens up The Edit Application Modal Window By using the Edit Icon"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User clicks on the "Edit" Button right on the left'
            clickOnEditButtonForFirstAssetDisplayed()

        then: 'The Option to edit every Option should be displayed'
            at AssetEditPage
        and: 'The appName should be displayed'
            aeModalAppName.value() ==  appName
    }

    def "6. Delete the Application already displayed "() {
        given: 'The User is on the Application Edition Section'
            at AssetEditPage
        when: 'The User Deletes the Application'
            deleteAsset()

        then: 'The user should be redirected to the Application List Section'
            at ViewPage
    }

    def "7. Validating the Application is not on the List"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User searches by the App already deleted'
            filterByName(appName)

        then: 'App should not be visible and Count should be equal to Zero'
            at ViewPage
            !verifyRowsDisplayed()
    }
}