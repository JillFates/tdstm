package specs.Assets.Application

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Assets.Application.ApplicationCreationPage
import pages.Assets.Application.ApplicationDetailsPage
import pages.Assets.Application.ApplicationEditionPage
import pages.Assets.Application.ApplicationListPage
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

    def "1. Filtering out by the Application already created"() {
        testKey = "TM-8492"
        given: 'The User displays the Menu Page'
            at MenuPage
        when: 'The User clicks in the Assets > Applications Menu option'
            assetsModule.goToApplications()

        then: 'Application list should be displayed'
            at ApplicationListPage
        when: 'The User searches by the App Name already created'
            waitFor {alNameFilter.click()}
            alNameFilter = appNameOld

        then: 'That App should be shown'
            commonsModule.waitForLoadingMessage()
            waitFor{alFirstAppName.text().trim() == appNameOld}
        when: 'The User clicks on that Application'
            waitFor{alFirstAppName.click()}

        then: 'The Application should be displayed'
            at ApplicationDetailsPage
    }

    def "2. Using the Edit and Cancel Buttons on dhe Application Modal Windows"() {
        testKey = "TM-8492"
        given: 'The User is on the Application Details Page'
            at ApplicationDetailsPage
        when: 'The User clicks the "Edit" Button'
            waitFor {adModalEditBtn.click()}

        then: 'The Option to edit every Option should be displayed'
            at ApplicationEditionPage
        when: 'The User clicks the "Cancel" Button'
            aeModalCancelBtn.click()

        then: 'The User should be redirected to the Application List Page'
            at ApplicationListPage
    }

    def "3. Using the Edit Button on the Application List section, right on the Left"() {
        testKey = "TM-8492"
        given: 'The User is on the Application List Page'
         at ApplicationListPage
        when: 'The User searches by the App Name already created'
            waitFor {alNameFilter.click()}
            alNameFilter = appNameOld

        then: 'That App should be shown'
            commonsModule.waitForLoadingMessage()
            waitFor{alFirstAppName.text().trim() == appNameOld}
        when: 'The User clicks on the "Edit" Button right on the left'
            waitFor{alFirstAppEdit.click()}

        then: 'The Option to edit every Option should be displayed'
            at ApplicationEditionPage
    }

    def "4. Edit Application - Changing Names and Static dropdowns"() {
        testKey = "TM-8492"
        given: 'The User is on the Application Edition Section'
            at ApplicationEditionPage
        when: 'The User changes the App Name, description, Bundle and Status values'
            aeModalAppName = appName
            aeModalDescription = appDesc
            aeModalBundleSelector.click()
            waitFor { aeModalBundleSelector.find("option", text: appBundle).first().click() }
            aeModalPlanStatusSelector.click()
            waitFor { aeModalPlanStatusSelector.find("option", text: appStatus).first().click() }

        then: 'Every value should be accordingly changed'
            aeModalAppName.value() == appName
            aeModalDescription.value() == appDesc
            aeModalBundleSelector.find("option", value:aeModalBundleSelector.value()).text() == appBundle
            aeModalPlanStatusSelector.find("option", value:aeModalPlanStatusSelector.value()).text() == appStatus
    }

    def "5. Edit Application - Changing dynamic Dropdowns"() {
        testKey = "TM-8492"
        given: 'The User is on the Application Edition Section'
            at ApplicationEditionPage
        when: 'The User changes the App SME1, SME2, appOwner values'
            aeModalSME1Selector.click()
            waitFor { aeModalSelectorValues.size() > 2 }
            appSME1 = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()-2))+2].text()
            aeModalSelectorValues.find("div", role: "option", text: appSME1).first().click()
            aeModalSME2Selector.click()
            waitFor { aeModalSelectorValues.size() > 2 }
            appSME2 = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()-2))+2].text()
            aeModalSelectorValues.find("div", role: "option", text: appSME2).first().click()
            aeModalAppOwnerSelector.click()
            waitFor { aeModalSelectorValues.size() > 2 }
            appOwner = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()-2))+2].text()
            aeModalSelectorValues.find("div", role: "option", text: appOwner).first().click()

        then: 'Every value should be accordingly changed'
            at ApplicationEditionPage
            aeModalSME1Selector.find("span",id: startsWith("select2-chosen")).text().trim() == appSME1
            aeModalSME2Selector.find("span",id: startsWith("select2-chosen")).text().trim() == appSME2
            aeModalAppOwnerSelector.find("span",id: startsWith("select2-chosen")).text().trim() == appOwner
    }

    def "6. Edit Application - Adding Support Dependencies"() {
        testKey = "TM-8492"
        given: 'The User is on the Application Edition Section'
            at ApplicationEditionPage
        when: 'The User clicks the "Support" Button'
            aeModalAddSuppBtn.click()

        then: 'A New Layout containing different Options should be displayed'
            waitFor { aeModalSuppColTitles[0].text().trim() == "Frequency" }
            aeModalSuppColTitles[1].text().trim() == "Class"
            aeModalSuppColTitles[2].text().trim() == "Name"
            aeModalSuppColTitles[3].text().trim() == "Bundle"
            aeModalSuppColTitles[4].text().trim() == "Type"
            aeModalSuppColTitles[5].text().trim() == "Status"
            aeModalSuppList.size() > 0
        when: 'The User completes information related to Frequency, Calls, Name, Bundle, Type and Status'
            aeModalSuppFreqSelector.click()
            waitFor { aeModalSuppFreqSelector.find("option", text: suppFreq).first().click() }
            aeModalSuppClassSelector.click()
            waitFor { aeModalSuppClassSelector.find("option", text: suppClass).first().click() }
            aeModalSuppNameSelector.click()
            waitFor { aeModalSelectorValues.size() > 2 }
            suppName = aeModalSelectorValues[Math.abs(new Random().nextInt() % (aeModalSelectorValues.size()))].text()
            waitFor { aeModalSelectorValues.find("div", role: "option", text: suppName).first().click() }
            aeModalSuppBundleSelector.click()
            waitFor { aeModalSuppBundleSelector.find("option", text: suppBundle).first().click() }
            aeModalSuppTypeSelector.click()
            waitFor { aeModalSuppTypeSelector.find("option", text: suppType).first().click() }
            aeModalSuppStatusSelector.click()
            waitFor { aeModalSuppStatusSelector.find("option", text: suppStatus).first().click() }

        then: 'Every value should be accordingly added'
            aeModalSuppFreqSelector.find("option", value: aeModalSuppFreqSelector.value()).text().trim() == suppFreq
            aeModalSuppClassSelector.find("option", value: aeModalSuppClassSelector.value()).text().trim() == suppClass
            aeModalSuppNameSelector.find("span", id: startsWith("select2-chosen")).text().trim() == suppName
            aeModalSuppBundleSelector.find("option", value: aeModalSuppBundleSelector.value()).text().trim() == suppBundle
            aeModalSuppTypeSelector.find("option", value: aeModalSuppTypeSelector.value()).text().trim() == suppType
            aeModalSuppStatusSelector.find("option", value: aeModalSuppStatusSelector.value()).text().trim() == suppStatus
    }

    def "7. Edit Application - Adding Dependencies"() {
        testKey = "TM-8492"
        given: 'The User is on the Application Edition Section'
            at ApplicationEditionPage
        when: 'The User clicks the "Dependency" Button'
            aeModalAddIsDepBtn.click()

        then: 'A New Layout containing different Options should be displayed'
            waitFor {aeModalIsDepColTitles[0].text().trim() == "Frequency"}
            aeModalIsDepColTitles[1].text().trim() == "Class"
            aeModalIsDepColTitles[2].text().trim() == "Name"
            aeModalIsDepColTitles[3].text().trim() == "Bundle"
            aeModalIsDepColTitles[4].text().trim() == "Type"
            aeModalIsDepColTitles[5].text().trim() == "Status"
            aeModalIsDepList.size() > 0
        when: 'The User completes information related to Frequency, Calls, Name, Bundle, Type and Status'
            aeModalIsDepFreqSelector.click()
            waitFor { aeModalIsDepFreqSelector.find("option", text: isDepFreq).first().click() }
            aeModalIsDepClassSelector.click()
            waitFor { aeModalIsDepClassSelector.find("option", text: isDepClass).first().click() }
            aeModalIsDepNameSelector.click()
            waitFor { aeModalSelectorValues.size() > 2 }
            isDepName = aeModalSelectorValues[Math.abs(new Random().nextInt()%(aeModalSelectorValues.size()))].text()
            waitFor { aeModalSelectorValues.find("div", role: "option", text: isDepName).first().click() }
            aeModalIsDepBundleSelector.click()
            waitFor { aeModalIsDepBundleSelector.find("option", text: isDepBundle).first().click() }
            aeModalIsDepTypeSelector.click()
            waitFor { aeModalIsDepTypeSelector.find("option", text: isDepType).first().click() }
            aeModalIsDepStatusSelector.click()
            isDepStatus = CommonActions.getRandomOption(aeModalIsDepStatusSelector.find("option")).text()
            waitFor { aeModalIsDepStatusSelector.find("option", text: isDepStatus).click() }

        then: 'Every value should be accordingly added'
            aeModalIsDepFreqSelector.find("option", value:aeModalIsDepFreqSelector.value()).text().trim() == isDepFreq
            aeModalIsDepClassSelector.find("option", value:aeModalIsDepClassSelector.value()).text().trim() == isDepClass
            aeModalIsDepNameSelector.find("span",id: startsWith("select2-chosen")).text().trim() == isDepName
            aeModalIsDepBundleSelector.find("option", value:aeModalIsDepBundleSelector.value()).text().trim() == isDepBundle
            aeModalIsDepTypeSelector.find("option", value:aeModalIsDepTypeSelector.value()).text().trim() == isDepType
            aeModalIsDepStatusSelector.find("option", value:aeModalIsDepStatusSelector.value()).text().trim() == isDepStatus
    }

    def "8. Edit Application - Save Changes and close modal"() {
        testKey = "TM-8492"
        given: 'The User is on the Application Edition Section'
            at ApplicationEditionPage
        when: 'The User clicks the "Update" Button'
            waitFor { aeModalUpdateBtn.click() }

        then: 'The User should be redirected to the Application Details Pop-Up'
            at ApplicationDetailsPage
        when: 'The User clicks the "Close" Button'
            waitFor { adModalCloseBtn.click() }

        then: 'The User should be redirected to the Application List Section'
            at ApplicationListPage
    }

    def "9. Filter edited Application on List"() {
        testKey = "TM-8492"
        given: 'The User is on the Application List Page'
            at ApplicationListPage
        when: 'The User searches by the Application already edited'
            waitFor {alNameFilter.click()}
            alNameFilter = appName
            waitFor{alFirstAppName.text().trim() == appName}
        and: 'The User clicks on that Application'
            waitFor{alFirstAppName.click()}

        then: 'The User should be redirected to the Application Details Page'
            at ApplicationDetailsPage
    }

    def "10. Validate Application Details"() {
        testKey = "TM-8492"
        when: 'The User is on the Application Details Page'
            at ApplicationDetailsPage

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