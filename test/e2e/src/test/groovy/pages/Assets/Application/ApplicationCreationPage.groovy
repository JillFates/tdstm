package pages.Assets.Application
import geb.Page

class ApplicationCreationPage extends Page {

    static at = {
        waitFor {acModalWindow.displayed}
        acModalTitle.text()             == "Application Create"
        acModalSaveBtn.text().trim()    == "Save"
        acModalCancelBtn.text().trim()  == "Cancel"
        acModalCloseBtn
    }

    static content = {

        acModalWindow                   (wait:true) { $("div","aria-describedby":"createEntityView")}
        acModalTitle                    { acModalWindow.find("span#ui-id-4") }
        acModalAppName                  { acModalWindow.find("input#assetName")}
        acModalDescription              { acModalWindow.find("input#description")}
        acModalSME1Selector(wait:true)  { acModalWindow.find("div#s2id_sme1")}
        acModalSME2Selector(wait:true)  { acModalWindow.find("div#s2id_sme2")}
        acModalAppOwnerSelector(wait:true) { acModalWindow.find("div#s2id_appOwner")}

        // TODO : The following element rasies when a modal dropdown is displayed  (eg: SME1,SM2, App Owner, etc) this should have a valid identifier
        acModalSelectorValues(wait:true, required:false) { $("div#select2-drop").find("li",class:"select2-results-dept-0 select2-result select2-result-selectable")}

        acModalBundleSelector           { acModalWindow.find("select#moveBundle")}
        acModalPlanStatusSelector       { acModalWindow.find("select#planStatus")}
        acModalValidationSelector       { acModalWindow.find("select#criticality")}
        acModalStartUpSelector          { acModalWindow.find("div#s2id_startupById")}
        acModalAddSupportBtn            { acModalWindow.find("input", type:"button","onclick":"EntityCrud.addAssetDependencyRow('support');")}
        acModalAddIsDepBtn              { acModalWindow.find("input", type:"button","onclick":"EntityCrud.addAssetDependencyRow('dependent');")}
        acModalSaveBtn                  { acModalWindow.find("button", "onclick":"EntityCrud.saveToShow(\$(this),'APPLICATION')")}
        acModalCancelBtn                { acModalWindow.find("button", "onclick":"EntityCrud.closeCreateModal();")}
        acModalCloseBtn                 { acModalWindow.find("button", class:"ui-dialog-titlebar-close")}
    }

    def createApplication(dataMap){
        waitFor {acModalAppName.displayed}

        acModalAppName = dataMap.appName

        acModalDescription = dataMap.appDesc

        acModalSME1Selector.click()
        def appSME1 = acModalSelectorValues[2].text()
        acModalSelectorValues.find("div", role:"option", text: appSME1).first().click()

        acModalSME2Selector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        def appSME2 = acModalSelectorValues[Math.floorDiv(acModalSelectorValues.size()-2,2)].text()
        acModalSelectorValues.find("div", role:"option", text: appSME2).first().click()

        acModalAppOwnerSelector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        def appOwner = acModalSelectorValues.last().text()
        acModalSelectorValues.find("div", role:"option", text: appOwner).first().click()

        acModalBundleSelector.click()
        acModalBundleSelector.find("option", text: dataMap.appBundle).click()

        acModalPlanStatusSelector.click()
        acModalPlanStatusSelector.find("option", text: dataMap.appStatus).click()

        acModalSaveBtn.click()
    }
}
