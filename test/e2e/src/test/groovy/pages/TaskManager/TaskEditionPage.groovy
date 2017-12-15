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

        etModalWindow(wait:true)    { $("div#editTaskPopup")}
        etModalTitle                { $("span#ui-id-5", class:"ui-dialog-title ng-binding") }
        etModalSaveBtn              { $("button#saveAndCloseBId")}
        etModalCancelBtn            { $("button",class:"btn btn-default tablesave cancel")}
        etModalNameTA(wait:true)    { $("#commentEditId")}
        etModalEdited(wait:true)    { $("#commentTdId")}
        etModalCloseBtn             { $("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}
        etModalEventSelector        { $("select#moveEvent")}

    }



}
