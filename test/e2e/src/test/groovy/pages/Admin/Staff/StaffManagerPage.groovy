package pages.Admin.Staff

import geb.Page

class StaffManagerPage extends Page {

    static at = {
        ctModalTitle.text() == "Create Task"
        ctModalSaveBtn.text().trim() == "Save"
        ctModalCancelBtn.text().trim() == "Cancel"
        ctModalCloseBtn
    }

    static content = {
        ctModalWindow(wait:true)        { $("div#editTaskPopup")}
        ctModalTitle                    { ctModalWindow.find("span#ui-id-5", class:"ui-dialog-title ng-binding")}
        ctModalTaskName(wait:true)      { ctModalWindow.find("#commentEditId")}
        ctModalEventSelector            { ctModalWindow.find("select#moveEvent")}
        ctModalStatusSelector(wait:true){ ctModalWindow.find("select#status")}
        ctModalSaveBtn                  { ctModalWindow.find("button#saveAndCloseBId")}
        ctModalCancelBtn                { ctModalWindow.find("button",class:"btn btn-default tablesave cancel")}
        ctModalCloseBtn                 { ctModalWindow.find("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}
    }
}
