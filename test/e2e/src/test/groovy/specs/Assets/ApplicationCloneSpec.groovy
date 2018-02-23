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
class ApplicationCloneSpec extends GebReportingSpec{

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
        }else {
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

    def "Open Clone Application Modal Window by Clone Button"() {
        testKey = "TM-8494"
        given:
        at ApplicationDetailsPage
        when:
        waitFor { adModalCloneBtn.displayed }
        waitFor { adModalCloneBtn.click() }
        then:
        at AssetClonePage
        waitFor { asclModalTitle.text().trim() == "Clone " + appName }
    }

    def "Close Clone modal window and Application detail modal window"() {
        testKey = "TM-8494"
        given:
        at AssetClonePage
        when:
        waitFor {asclModalCloseBtn.click()}
        then:
        at ApplicationDetailsPage
        adModalTitle.text() == appName + " Detail"
        when:
        waitFor {adModalCloseBtn.click()}
        then:
        at ApplicationListPage
    }

    def "Filter Applications on List again using the app name"() {
        testKey = "TM-8494"
        given:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = appName
        waitFor {alLoadingGrid.displayed}
        waitFor {!alLoadingGrid.displayed}
        waitFor{alFirstAppName.text().trim() == appName}
        then:
        at ApplicationListPage
    }

    def "Open Clone Application Modal Window By Clone Icon"() {
        testKey = "TM-8494"
        given:
        at ApplicationListPage
        when:
        waitFor {alFirstAppClone.click()}
        then:
        at AssetClonePage
        waitFor { asclModalTitle.text().trim() == "Clone " + appName }

    }

    def "Verify Cloned Asset name and Error legend"() {
        testKey = "TM-8494"
        when:
        at AssetClonePage
        then:
        asclModalAssetCloneName.value() == appName
        asclModalErrorMsg.text().trim() == "Change name appropriately"
    }

    def "Click Clone & EditButton and verify confirm legend"() {
        testKey = "TM-8494"
        when:
        at AssetClonePage
        then:
        asclModalCloneEditBtn.click()
        asclModalDialogTitle.text().trim() == "Asset already exists"
        asclModalDialogText.text().trim() == "The Asset Name you want to create already exists, do you want to proceed?"
    }

    def "Cancel Assset Clone and verify the Asset is not cloned"() {
        testKey = "TM-8494"
        given:
        at AssetClonePage
        when:
        waitFor {asclModalDialogCancelbtn.click()}
        waitFor {asclModalCancelBtn.click()}
        then:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = appName
        waitFor {alLoadingGrid.displayed}
        waitFor {!alLoadingGrid.displayed}
        waitFor{alFirstAppName.text().trim() == appName}
        then:
        at ApplicationListPage
        waitFor{alGridRows.size() == appCountBefore}
    }

    def "Open Clone Asset again and confirm the Clone & Edit with same name"() {
        testKey = "TM-8494"
        given:
        at ApplicationListPage
        when:
        waitFor {alFirstAppClone.click()}
        then:
        at AssetClonePage
        waitFor { asclModalTitle.text().trim() == "Clone " + appName }
        when:
        waitFor {asclModalCloneEditBtn.click()}
        then:
        waitFor {asclModalDialog.displayed}
        when:
        waitFor {asclModalDialogConfirmBtn.click()}
        then:
        at ApplicationEditionPage
        waitFor {aeModalAppName.value() == appName}
        waitFor {!aeModalUpdateBtn.@disabled}
        when:
        waitFor {aeModalUpdateBtn.click()}
        then:
        at ApplicationDetailsPage
    }

    def "Validate Application Details"() {
        testKey = "TM-8494"
        when:
        at ApplicationDetailsPage
        then:
// TODO some Application Detail items can be reached because cannot be identified by itself. Will chaneg this feature after FE code has reviewed
        waitFor{adModalAppName[1].text().trim() == appName}
        waitFor{adModalCloseBtn.click()}
    }

    def "Check App List the duplicated item"() {
        testKey = "TM-8494"
        given:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = appName
        then:
        waitFor {alLoadingGrid.displayed}
        waitFor {!alLoadingGrid.displayed}
        waitFor{alGridRows.size() == appCountBefore + 1}
    }

    def "Open Cloned Application Modal Window By Edit Icon"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationListPage
        when:
        waitFor {alFirstAppEdit.click()}
        then:
        at ApplicationEditionPage
        aeModalAppName.value() ==  appName
    }

}

