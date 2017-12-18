package pages.TaskManager
import geb.Page

class TaskEditionPage extends Page{

    static at = {
        etModalTitle.text() == "Edit Task"
        etModalSaveBtn.text().trim() == "Save"
        etModalCancelBtn.text().trim() == "Cancel"
        etModalCloseBtn
    }

    static content = {

        etModalWindow(wait:true)        { $("div#editTaskPopup")}
        etModalTitle                    { etModalWindow.find("span#ui-id-5", class:"ui-dialog-title ng-binding") }
        etModalTaskName(wait:true)      { etModalWindow.find("#commentEditId")}
        etModalEventSelector            { etModalWindow.find("select#moveEvent")}
        etModalStatusSelector(wait:true){ etModalWindow.find("select#status")}
        etModalSaveBtn                  { etModalWindow.find("button#saveAndCloseBId")}
        etModalCancelBtn                { etModalWindow.find("button",class:"btn btn-default tablesave cancel")}
        etModalCloseBtn                 { etModalWindow.find("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}
    }



}
