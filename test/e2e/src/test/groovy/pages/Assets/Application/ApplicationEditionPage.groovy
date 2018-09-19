package pages.Assets.Application
import geb.Page

class ApplicationEditionPage extends Page {

    static at = {
        waitFor {aeModalWindow.displayed}
        aeModalTitle.text()             == "Application Edit"
        aeModalUpdateBtn.value()        == "Update"
        aeModalDeleteBtn.value()        == "Delete"
        aeModalCancelBtn.value()        == "Cancel"
        aeModalCloseBtn
    }

    static content = {
        aeModalWindow                   (wait:true) { $("div","aria-describedby":"editEntityView")}
        aeModalTitle                    { aeModalWindow.find("span#ui-id-6")}
        aeModalAppName                  { aeModalWindow.find("input#assetName")}
        aeModalDescription              { aeModalWindow.find("input#description")}
        aeModalSME1Selector             (wait:true) { aeModalWindow.find("div#s2id_sme1")}
        aeModalSME2Selector             (wait:true) { aeModalWindow.find("div#s2id_sme2")}
        aeModalAppOwnerSelector         (wait:true) { aeModalWindow.find("div#s2id_appOwnerEdit")}

        // TODO : The following elements rasies when a modal dropdown is displayed  (eg: SME1,SM2, App Owner, Support Asset Name, Is dependent Name, etc) this should have a valid identifier
        aeModalSelector                 (wait:true, required:false) { $("div#select2-drop")}
        aeModalSelectorValues           { aeModalSelector.find("li",class:"select2-results-dept-0 select2-result select2-result-selectable")}

        aeModalBundleSelector           { aeModalWindow.find("select#moveBundle")}
        aeModalPlanStatusSelector       { aeModalWindow.find("select#planStatus")}
        aeModalValidationSelector       { aeModalWindow.find("select#criticality")}
        aeModalStartUpSelector          { aeModalWindow.find("div#s2id_startupById")}
        aeModalDependencyTable          { aeModalWindow.find("tr#applicationDependentId")}

        aeModalAddSuppBtn               { aeModalDependencyTable.find("input", type:"button","onclick":"EntityCrud.addAssetDependencyRow('support');")}
        aeModalSuppColTitles            (wait:true, required:false) { aeModalDependencyTable.find("div",0).find("table thead th")}
        aeModalSuppList                 (required:false) { aeModalDependencyTable.find("tbody#supportList").find("tr")}
        aeModalSuppFirstDDs             (required:false) { aeModalSuppList.first().find("td")}
        aeModalSuppFreqSelector         (required:false) { aeModalSuppFirstDDs.find("select", id:startsWith("dataFlowFreq_support"))}
        aeModalSuppClassSelector        (required:false) { aeModalSuppFirstDDs.find("select", id:startsWith("entity_support"))}
        aeModalSuppNameSelector         (wait:true, required:false) { aeModalSuppFirstDDs.find("div", id:startsWith("s2id_asset_support"))}
        aeModalSuppBundleSelector       (required:false) { aeModalSuppFirstDDs.find("select", id:startsWith("moveBundle_support"))}
        aeModalSuppTypeSelector         (required:false) { aeModalSuppFirstDDs.find("select", id:startsWith("type_support"))}
        aeModalSuppStatusSelector       (required:false) { aeModalSuppFirstDDs.find("select", id:startsWith("status_support"))}

        aeModalAddIsDepBtn              { aeModalDependencyTable.find("input", type:"button","onclick":"EntityCrud.addAssetDependencyRow('dependent');")}
        aeModalIsDepColTitles           (wait:true, required:false) { aeModalDependencyTable.find("div",0).find("table thead th")}
        aeModalIsDepList                (wait:true, required:false) { aeModalDependencyTable.find("tbody#dependentList").find("tr")}
        aeModalIsDepFirstDDs            (required:false) { aeModalIsDepList.first().find("td")}
        aeModalIsDepFreqSelector        (required:false){ aeModalIsDepFirstDDs.find("select", id:startsWith("dataFlowFreq_dependent"))}
        aeModalIsDepClassSelector       (required:false){ aeModalIsDepFirstDDs.find("select", id:startsWith("entity_dependent"))}
        aeModalIsDepNameSelector        (wait:true, required:false) { aeModalIsDepFirstDDs.find("div", id:startsWith("s2id_asset_dependent"))}
        aeModalIsDepBundleSelector      (required:false){ aeModalIsDepFirstDDs.find("select", id:startsWith("moveBundle_dependent"))}
        aeModalIsDepTypeSelector        (required:false){ aeModalIsDepFirstDDs.find("select", id:startsWith("type_dependent"))}
        aeModalIsDepStatusSelector      (required:false){ aeModalIsDepFirstDDs.find("select", id:startsWith("status_dependent"))}

        aeModalUpdateBtn                (wait:true, required:false) { aeModalWindow.find("input#assetUpdateButton")}
        aeModalDeleteBtn                { aeModalWindow.find("input", type:"submit", name:"_action_Delete")}
        aeModalCancelBtn                { aeModalWindow.find("input", type:"button", onclick:"\$('#editEntityView').dialog('close');")}
        aeModalCloseBtn                 { aeModalWindow.find("button", class:"ui-dialog-titlebar-close")}
    }
}
