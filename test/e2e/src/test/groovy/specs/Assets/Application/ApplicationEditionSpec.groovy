package specs.Assets.Application

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Assets.AssetViews.AssetCreatePage
import pages.Assets.AssetViews.AssetDetailsPage
import pages.Assets.AssetViews.AssetEditPage
import pages.Assets.AssetViews.ViewPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class ApplicationEditionSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the app you will Create and Edit
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static appNameOld = baseName +" "+ randStr + " App For E2E Created"
    static appName = baseName +" "+ randStr + " App For E2E Edited"
    static appDescOld = baseName +" "+ randStr + " App Description Created"
    static appDesc = baseName +" "+ randStr + " App Description Edited"
    static appSME1
    static appSME2
    static appOwner
    static appBundleOld = "Buildout"
    static appBundle = "M1-Phy"
    static appStatusOld = "Assigned"
    static appStatus = "Confirmed"
    static suppFreq = "constant"
	static suppClass = "Applications"
    static suppName
	static suppBundle = appBundle
    static suppType = "Backup"
	static suppStatus = "Validated"
    static isDepFreq = "daily"
    static isDepClass = "Databases"
    static isDepName
    static isDepBundle = appBundle
    static isDepType = "Batch"
    static isDepStatus = "Confirmed"

    static appDataMap = [
            appName: appNameOld,
            appDesc: appDescOld,
            appBundle: appBundleOld,
            appStatus: appStatusOld
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
        clickOnCloseButton()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Filtering out by the Application already created"() {
        given: 'The User displays the Menu Page'
            at MenuPage
        when: 'The User clicks in the Assets > Applications Menu option'
            assetsModule.goToApplications()

        then: 'Application list should be displayed'
            at ViewPage
        when: 'The User searches by the App Name already created'
            filterByName appNameOld
        and: 'The User clicks on that Application'
            openFirstAssetDisplayed()

        then: 'The Application should be displayed'
            at AssetDetailsPage
    }

    def "2. Using the Edit and Cancel Buttons on the Application Modal Windows"() {
        given: 'The User is on the Application Details Page'
            at AssetDetailsPage
        when: 'The User clicks the "Edit" Button'
            waitFor {editButton.click()}

        then: 'The Option to edit every Option should be displayed'
            at AssetEditPage
        when: 'The User clicks the "Cancel" Button'
            clickOnCloseButton()

        then: 'The User should be redirected to the Application List Page'
            at ViewPage
    }

    def "3. Using the Edit Button on the Application List section,on the leftmost column"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User searches by the App Name already created'
            filterByName appNameOld
        and: 'The User clicks on the "Edit" Button on the leftmost column'
            clickOnEditButtonForFirstAssetDisplayed()

        then: 'The Option to edit every Option should be displayed'
            at AssetEditPage
    }

    def "4. Edit Application - Changing Names and Static dropdowns"() {
        given: 'The User is on the Application Edition Section'
            at AssetEditPage
        when: 'The User changes the App Name, description, Bundle and Status values'
            aeModalAppName = appName
            aeModalDescription = appDesc
            aeModalBundleArrow.click()
            waitFor { aeModalSelectorValues.find{it.text()==appBundle}.click() }
            aeModalPlanStatusArrow.click()
            waitFor { aeModalSelectorValues.find{it.text()==appStatus}.click() }

        then: 'Every value should be accordingly changed'
            aeModalAppName.value() == appName
            aeModalDescription.value() == appDesc
            aeModalBundleValue.text() == appBundle
            aeModalPlanStatusValue.text() == appStatus
    }

    def "5. Edit Application - Changing dynamic Dropdowns"() {
        given: 'The User is on the Application Edition Section'
            at AssetEditPage
        when: 'The User changes the App SME1, SME2, appOwner values'
            aeModalSME1Arrow.click()
            waitFor { aeModalSelectorValues.size() > 2 }
            def selector = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()-2))+2]
            appSME1 = selector.text()
            selector.click()
            aeModalSME2Arrow.click()
            waitFor { aeModalSelectorValues.size() > 2 }
            selector = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()-2))+2]
            appSME2 = selector.text()
            selector.click()
            aeModalAppOwnerArrow.click()
            waitFor { aeModalSelectorValues.size() > 2 }
            selector = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()-2))+2]
            appOwner = selector.text()
            selector.click()

        then: 'Every value should be accordingly changed'
            at AssetEditPage
            aeModalSME1Value.text().trim() == appSME1
            aeModalSME2Value.text().trim() == appSME2
            aeModalAppOwnerValue.text().trim() == appOwner
    }

    def "6. Edit Application - Adding Support Dependencies"() {
        given: 'The User is on the Application Edition Section'
            at AssetEditPage
        when: 'The User clicks the "Support" Button'
            commonsModule.goToElement(saveButton) // avoid element not found
            aeModalAddSuppBtn.click()

        then: 'A New Layout containing different Options should be displayed'
            waitFor { aeModalSuppColTitles[1].text().trim() == "Frequency" }
            aeModalSuppColTitles[2].text().trim() == "Class"
            aeModalSuppColTitles[3].text().trim() == "Name"
            aeModalSuppColTitles[4].text().trim() == "Bundle"
            aeModalSuppColTitles[5].text().trim() == "Type"
            aeModalSuppColTitles[6].text().trim() == "Status"
            aeModalSuppList.size() > 0
        when: 'The User completes information related to Frequency, Calls, Name, Bundle, Type and Status'
            waitFor { aeModalSuppFreqArrow.click()}
            waitFor { aeModalSelectorValues.find{it.text()==suppFreq}.click() }
            waitFor { aeModalSuppClassArrow.click()}
            waitFor { aeModalSelectorValues.find{it.text()==suppClass}.click() }
            waitFor { aeModalSuppNameArrow.click()}
            commonsModule.waitForLoader(2) // assets list is loaded
            waitFor { aeModalSelectorValues.size() > 2 }
            def selector = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()-2))+2]
            commonsModule.waitForLoader(2)
            suppName = selector.text()
            commonsModule.waitForLoader(3)
            selector.click()
            commonsModule.waitForLoader(3) // bundle associated to asset is loaded
            aeModalSuppBundleArrow.click()
            commonsModule.waitForLoader(3)
            waitFor { aeModalSelectorValues.find{it.text()==suppBundle}.click() }
            waitFor {aeModalSuppTypeArrow.click()}
            waitFor { aeModalSelectorValues.find{it.text()==suppType}.click() }
            waitFor {aeModalSuppStatusArrow.click()}
            waitFor { aeModalSelectorValues.find{it.text()==suppStatus}.click() }

        then: 'Every value should be accordingly added'
            aeModalSuppFreqValue.text().trim() == suppFreq
            aeModalSuppClassValue.text().trim() == suppClass
            aeModalSuppNameValue.value() == suppName
            aeModalSuppBundleValue.text().trim() == suppBundle
            aeModalSuppTypeValue.text().trim() == suppType
            aeModalSuppStatusValue.text().trim() == suppStatus
    }

    def "7. Edit Application - Adding Dependencies"() {
        given: 'The User is on the Application Edition Section'
            at AssetEditPage
        when: 'The User clicks the "Dependency" Button'
            aeModalAddIsDepBtn.click()

        then: 'A New Layout containing different Options should be displayed'
            waitFor {aeModalIsDepColTitles[1].text().trim() == "Frequency"}
            aeModalIsDepColTitles[2].text().trim() == "Class"
            aeModalIsDepColTitles[3].text().trim() == "Name"
            aeModalIsDepColTitles[4].text().trim() == "Bundle"
            aeModalIsDepColTitles[5].text().trim() == "Type"
            aeModalIsDepColTitles[6].text().trim() == "Status"
            aeModalIsDepList.size() > 0
        when: 'The User completes information related to Frequency, Calls, Name, Bundle, Type and Status'
            aeModalIsDepFreqArrow.click()
            waitFor { aeModalSelectorValues.find{it.text()==isDepFreq}.click() }
            aeModalIsDepClassArrow.click()
            waitFor { aeModalSelectorValues.find{it.text()==isDepClass}.click() }
            aeModalIsDepNameArrow.click()
            commonsModule.waitForLoader(2) // assets list is loaded
            waitFor { aeModalSelectorValues.size() > 2 }
            def selector = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()-2))+2]
            isDepName = selector.text()
            selector.click()
            commonsModule.waitForLoader(1) // bundle associated to asset is loaded
            aeModalIsDepBundleArrow.click()
            waitFor { aeModalSelectorValues.find{it.text()==isDepBundle}.click() }
            aeModalIsDepTypeArrow.click()
            waitFor { aeModalSelectorValues.find{it.text()==isDepType}.click() }
            aeModalIsDepStatusArrow.click()
            waitFor { aeModalSelectorValues.find{it.text()==isDepStatus}.click() }

        then: 'Every value should be accordingly added'
            aeModalIsDepFreqValue.text().trim() == isDepFreq
            aeModalIsDepClassValue.text().trim() == isDepClass
            aeModalIsDepNameValue.value() == isDepName
            aeModalIsDepBundleValue.text().trim() == isDepBundle
            aeModalIsDepTypeValue.text().trim() == isDepType
            aeModalIsDepStatusValue.text().trim() == isDepStatus
    }

    def "8. Edit Application - Save Changes and close modal"() {
        given: 'The User is on the Application Edition Section'
            at AssetEditPage
        when: 'The User clicks the "Save" Button'
            clickOnSaveButton()

        then: 'The User should be redirected to the Application Details Pop-Up'
            at AssetDetailsPage
        when: 'The User clicks the "Close" Button'
            clickOnCloseButton()

        then: 'The User should be redirected to the Application List Section'
            at ViewPage
    }

    def "9. Filter edited Application on List"() {
        given: 'The User is on the Application List Page'
            at ViewPage
        when: 'The User searches by the Application already edited'
            filterByName(appName)
        and: 'The User clicks on that Application'
            openFirstAssetDisplayed()

        then: 'The User should be redirected to the Application Details Page'
            at AssetDetailsPage
    }

    def "10. Validate Application Details"() {
        when: 'The User is on the Application Details Page'
            at AssetDetailsPage

        then: 'New AppName, SME1, SME2 and AppOwner values should be displayed'
            // TODO following items are located by array instead of itself because cannot be identified
            waitFor{adModalAppName.text().trim() == appName}
            // TODO some items cannot be located due to missing ID's
            adModalSME1.text().trim().contains(appSME1)
            adModalSME2.text().trim().contains(appSME2)
            adModalAppOwner.text().trim().contains(appOwner)
            // TODO Dependency elements are unreachable due to missing ID's
    }
}