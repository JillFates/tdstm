package pages.Tasks.TaskManager

import geb.Page
import modules.CommonsModule
import modules.TasksMenuModule
import pages.Tasks.*

class TaskCreationPage extends Page {

    static at = {
        tcModalTitle.text() == "Create Task"
        tcModalSaveBtn.text().trim() == "Save"
        tcModalCancelBtn.text().trim() == "Cancel"
        tcModalCloseBtn
    }

    static content = {
        tcModalWindow(wait:true) { $("div#editTaskPopup")}
        tcModalLoading (wait:true) { tcModalWindow.find("div", class:"loading-indicator", "ng-show":"isLoading")}
        tcModalTitle { tcModalWindow.find("span#ui-id-5", class:"ui-dialog-title ng-binding") }
        tcModalTaskName(wait:true) { tcModalWindow.find("#commentEditId")}
        tcModalPersonSelector(wait:true){ tcModalWindow.find("select#assignedTo")}
        tcModalTeamSelector(wait:true) { tcModalWindow.find("select#roleType")}
        tcModalEventSelector { tcModalWindow.find("select#moveEvent")}
        tcModalCategorySelector { tcModalWindow.find("select#category")}
        tcModalAssetTypeSelector { tcModalWindow.find("select","ng-change":"assetClassChanged()")}
        tcModalAssetNameSelector { tcModalWindow.find("div#s2id_currentAsset")}
        tcModalAssetNameSelValues(wait:true, required:false) { $("div#select2-drop").find("li",class:"select2-results-dept-0 select2-result select2-result-selectable")}
        tcModalAssetNameMoreResults (wait:true, required:false) { tcModalAssetNameSelValues.find("li.select2-more-results")}
        tcModalStatusSelector(wait:true){ tcModalWindow.find("select#status")}
        tcModalAddPredecessor(wait:true){ tcModalWindow.find("a","ng-class":"{'btn-default':hoverPredecessor}")}
        tcModalAddSuccessor(wait:true) { tcModalWindow.find("a","ng-class":"{'btn-default':hoverSuccessor}")}
        tcModalSaveBtn { tcModalWindow.find("button#saveAndCloseBId")}
        tcModalCancelBtn { tcModalWindow.find("button",class:"btn btn-default tablesave cancel")}
        tcModalCloseBtn { tcModalWindow.find("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}
        commonsModule { module CommonsModule }
        tasksModule { module TasksMenuModule}
    }
}
