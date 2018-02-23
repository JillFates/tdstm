package specs.Assets

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.Assets.ApplicationCreationPage
import pages.Assets.ApplicationDetailsPage
import pages.Assets.ApplicationEditionPage
import pages.Assets.ApplicationListPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class ApplicationEditionSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the app you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
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

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { menuModule.goToApplications() }
        at ApplicationListPage
        waitFor { alCreateAppBtn.click() }
        at ApplicationCreationPage
        acModalAppName = appNameOld
        acModalDescription = appDescOld
        acModalSME1Selector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        appSME1 = acModalSelectorValues[2].text()
        acModalSelectorValues.find("div", role:"option", text: appSME1).first().click()
        acModalSME2Selector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        appSME2 = acModalSelectorValues[Math.floorDiv(acModalSelectorValues.size()-2,2)].text()
        acModalSelectorValues.find("div", role:"option", text: appSME2).first().click()
        acModalAppOwnerSelector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        appOwner = acModalSelectorValues.last().text()
        acModalSelectorValues.find("div", role:"option", text: appOwner).first().click()
        acModalBundleSelector.click()
        acModalBundleSelector.find("option", text: appBundleOld).first().click()
        acModalPlanStatusSelector.click()
        acModalPlanStatusSelector.find("option", text: appStatusOld).first().click()
        acModalSaveBtn.click()
        at ApplicationDetailsPage
        waitFor { adModalCloseBtn.click() }
        at ApplicationListPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "Go To Asset Applications and filter created Application on List"() {
        testKey = "TM-8492"
        given:
        at MenuPage
        when:
        menuModule.goToApplications()
        then:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = appNameOld
        then:
        waitFor {alLoadingGrid.displayed}
        waitFor {!alLoadingGrid.displayed}
        waitFor{alFirstAppName.text().trim() == appNameOld}
        when:
        waitFor{alFirstAppName.click()}
        then:
        at ApplicationDetailsPage
    }

    def "Open Edit Application Modal Window by Edit Button and Cancel"() {
        testKey = "TM-8492"
        given:
        at ApplicationDetailsPage
        when:
        waitFor {adModalEditBtn.click()}
        then:
        at ApplicationEditionPage
        when:
        aeModalCancelBtn.click()
        then:
        at ApplicationListPage
    }

    def "filter created Application on List and open de Application edit modal window by icon en left"() {
        testKey = "TM-8492"
        given:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = appNameOld
        then:
        waitFor {alLoadingGrid.displayed}
        waitFor {!alLoadingGrid.displayed}
        waitFor{alFirstAppName.text().trim() == appNameOld}
        when:
        waitFor{alFirstAppEdit.click()}
        then:
        at ApplicationEditionPage
    }

    def "Edit Application - Change Names and Static dropdowns"() {
        testKey = "TM-8492"
        given:
        at ApplicationEditionPage
        when:
        aeModalAppName = appName
        aeModalDescription = appDesc
        aeModalBundleSelector.click()
        waitFor { aeModalBundleSelector.find("option", text: appBundle).first().click() }
        aeModalPlanStatusSelector.click()
        waitFor { aeModalPlanStatusSelector.find("option", text: appStatus).first().click() }
        then:
        aeModalAppName.value() == appName
        aeModalDescription.value() == appDesc
        aeModalBundleSelector.find("option", value:aeModalBundleSelector.value()).text() == appBundle
        aeModalPlanStatusSelector.find("option", value:aeModalPlanStatusSelector.value()).text() == appStatus
    }

    def "Edit Application - Change dynamic Dropdowns"() {
        testKey = "TM-8492"
        given:
        at ApplicationEditionPage
        when:
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
        then:
        at ApplicationEditionPage
        aeModalSME1Selector.find("span",id: startsWith("select2-chosen")).text().trim() == appSME1
        aeModalSME2Selector.find("span",id: startsWith("select2-chosen")).text().trim() == appSME2
        aeModalAppOwnerSelector.find("span",id: startsWith("select2-chosen")).text().trim() == appOwner

    }

    def "Edit Application - add Support"() {
        testKey = "TM-8492"
        given:
        at ApplicationEditionPage
        when:
        aeModalAddSuppBtn.click()
        then:
        waitFor { aeModalSuppColTitles[0].text().trim() == "Frequency" }
        aeModalSuppColTitles[1].text().trim() == "Class"
        aeModalSuppColTitles[2].text().trim() == "Name"
        aeModalSuppColTitles[3].text().trim() == "Bundle"
        aeModalSuppColTitles[4].text().trim() == "Type"
        aeModalSuppColTitles[5].text().trim() == "Status"
        aeModalSuppList.size() > 0
        when:
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
        then:
        aeModalSuppFreqSelector.find("option", value: aeModalSuppFreqSelector.value()).text().trim() == suppFreq
        aeModalSuppClassSelector.find("option", value: aeModalSuppClassSelector.value()).text().trim() == suppClass
        aeModalSuppNameSelector.find("span", id: startsWith("select2-chosen")).text().trim() == suppName
        aeModalSuppBundleSelector.find("option", value: aeModalSuppBundleSelector.value()).text().trim() == suppBundle
        aeModalSuppTypeSelector.find("option", value: aeModalSuppTypeSelector.value()).text().trim() == suppType
        aeModalSuppStatusSelector.find("option", value: aeModalSuppStatusSelector.value()).text().trim() == suppStatus
    }

    def "Edit Application - add Is Dependent on"() {
        testKey = "TM-8492"
        given:
        at ApplicationEditionPage
        when:
        aeModalAddIsDepBtn.click()
        then:
        waitFor {aeModalIsDepColTitles[0].text().trim() == "Frequency"}
        aeModalIsDepColTitles[1].text().trim() == "Class"
        aeModalIsDepColTitles[2].text().trim() == "Name"
        aeModalIsDepColTitles[3].text().trim() == "Bundle"
        aeModalIsDepColTitles[4].text().trim() == "Type"
        aeModalIsDepColTitles[5].text().trim() == "Status"
        aeModalIsDepList.size() > 0
        when:
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
        waitFor { aeModalIsDepStatusSelector.find("option", text: isDepStatus).first().click() }
        then:
        aeModalIsDepFreqSelector.find("option", value:aeModalIsDepFreqSelector.value()).text().trim() == isDepFreq
        aeModalIsDepClassSelector.find("option", value:aeModalIsDepClassSelector.value()).text().trim() == isDepClass
        aeModalIsDepNameSelector.find("span",id: startsWith("select2-chosen")).text().trim() == isDepName
        aeModalIsDepBundleSelector.find("option", value:aeModalIsDepBundleSelector.value()).text().trim() == isDepBundle
        aeModalIsDepTypeSelector.find("option", value:aeModalIsDepTypeSelector.value()).text().trim() == isDepType
        aeModalIsDepStatusSelector.find("option", value:aeModalIsDepStatusSelector.value()).text().trim() == isDepStatus
    }

    def "Edit Application - Save Changes and close modal"() {
        testKey = "TM-8492"
        given:
        at ApplicationEditionPage
        when:
        waitFor { aeModalUpdateBtn.click() }
        then:
        at ApplicationDetailsPage
        when:
        waitFor { adModalCloseBtn.click() }
        then:
        at ApplicationListPage
    }

    def "Filter edited Application on List"() {
        testKey = "TM-8492"
        given:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = appName
        waitFor{alFirstAppName.text().trim() == appName}
        waitFor{alFirstAppName.click()}
        then:
        at ApplicationDetailsPage
    }

    def "Validate Application Details"() {
        testKey = "TM-8492"
        when:
        at ApplicationDetailsPage
        then:
        // TODO following items are located by array instead of itself because cannot be identified
        waitFor{adModalAppName[1].text().trim() == appName}
        // TODO some items cannot be located due to missing ID's
        adModalSME1.text().trim().contains(appSME1)
        adModalSME2.text().trim().contains(appSME2)
        adModalAppOwner.text().trim().contains(appOwner)
        // TODO Dependency elements are unreachable due to missing ID's
    }
}