package pages.Tasks.TaskManager

import geb.Page

class TaskEditionPage extends Page{

    static at = {
        teModalTitle.text() == "Edit Task"
        teModalSaveBtn.text().trim() == "Save"
        teModalCancelBtn.text().trim() == "Cancel"
        teModalCloseBtn
    }

    static content = {
        teModalWindow(wait:true)        { $("div#editTaskPopup")}
        teModalLoading (wait:true)      { teModalWindow.find("div", class:"loading-indicator", "ng-show":"isLoading")}
        teModalTitle                    { teModalWindow.find("span#ui-id-5", class:"ui-dialog-title ng-binding") }
        teModalTaskName(wait:true)      { teModalWindow.find("#commentEditId")}
        teModalPersonSelector(wait:true){ teModalWindow.find("select#assignedTo")}
        teModalTeamSelector(wait:true)  { teModalWindow.find("select#roleType")}
        teModalEventSelector            { teModalWindow.find("select#moveEvent")}
        teModalCategorySelector         { teModalWindow.find("select#category")}
        teModalAssetTypeSelector        { teModalWindow.find("select","ng-change":"assetClassChanged()")}
        teModalAssetNameSelector        { teModalWindow.find("div#s2id_currentAsset")}
        teModalAssetNameSelValues(wait:true, required:false) { $("div#select2-drop").find("li",class:"select2-results-dept-0 select2-result select2-result-selectable")}
        teModalAssetNameMoreResults (wait:true, required:false) { teModalAssetNameSelValues.find("li.select2-more-results")}
        teModalInstructionsLink         {teModalWindow.find("input#instructionsLinkId")}
        teModalStatusSelector(wait:true) { teModalWindow.find("select#status")}
        teModalAddPredecessorBtn(wait:true) { teModalWindow.find("a","ng-click":"\$broadcast('addDependency','predecessor')")}
        teModalPredecessorDD (wait:true, required:false){ teModalWindow.find("task-dependencies","ng-model":"dependencies.predecessors").find("span",class:"k-icon k-i-arrow-s")}
        teModalAddSuccessorBtn(wait:true) { teModalWindow.find("a","ng-click":"\$broadcast('addDependency','successor')")}
        teModalSuccessorDD  (wait:true, required:false) { teModalWindow.find("task-dependencies","ng-model":"dependencies.successors").find("span",class:"k-icon k-i-arrow-s")}
        teModalNote                     { teModalWindow.find("textarea#noteEditId")}
        teModalSaveBtn                  { teModalWindow.find("button#saveAndCloseBId")}
        teModalCancelBtn                { teModalWindow.find("button",class:"btn btn-default tablesave cancel")}
        teModalCloseBtn                 { teModalWindow.find("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}
        teModalPredecessorUl {$('.k-animation-container')[0].find("ul", "aria-hidden":"false")}
        teModalSuccessorUl {$('.k-animation-container')[1].find("ul", "aria-hidden":"false")}
        teModalPredecessorOptions {teModalPredecessorUl.find("li")}
        teModalSuccessorOptions {teModalSuccessorUl.find("li")}
    }
}
