package specs.Assets

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.Assets.ApplicationCreationPage
import pages.Assets.ApplicationDetailsPage
import pages.Assets.ApplicationListPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class ApplicationCreationSpec extends GebReportingSpec{

    def testKey
    static testCount
    //Define the names of the app you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
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

    def "Go To Asset Applications"() {
        testKey = "TM-XXXX"
        given:
        at MenuPage
        when:
        menuModule.goToApplications()
        then:
        at ApplicationListPage
    }

    def "Open Create Application Modal Window"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationListPage
        when:
        alCreateAppBtn.click()
        then:
        at ApplicationCreationPage
    }

    def "Create Application"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationCreationPage
        when:
        acModalAppName = appName
        acModalDescription = appDesc
        acModalSME1Selector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        appSME1 = acModalSelectorValues[2].text()
        waitFor { acModalSelectorValues.find("div", role:"option", text: appSME1).click() }
        acModalSME2Selector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        appSME2 = acModalSelectorValues[Math.floorDiv(acModalSelectorValues.size()-1,2)].text()
        waitFor { acModalSelectorValues.find("div", role:"option", text: appSME2).click() }
        acModalAppOwnerSelector.click()
        appOwner = acModalSelectorValues[acModalSelectorValues.size()-1].text()
        waitFor { acModalSelectorValues.size() > 2 }
        waitFor { acModalSelectorValues.find("div", role:"option", text: appOwner).click() }
        acModalBundleSelector = appBundle
        acModalPlanStatusSelector = appStatus
        waitFor { acModalSaveBtn.click() }
        then:
        at ApplicationDetailsPage
        waitFor { adModalCloseBtn.click() }
        at ApplicationListPage
    }

    def "Filter Application on List"() {
        testKey = "TM-XXXX"
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
        testKey = "TM-XXXX"
        when:
        at ApplicationDetailsPage
        then:
// TODO some items cannot located due to missing ID's
        waitFor{adModalAppName[1].text().trim() == appName}
        adModalSME1.text().trim() == appSME1
        adModalSME2.text().trim() == appSME2
        adModalAppOwner.text().trim() == appOwner

    }

}

