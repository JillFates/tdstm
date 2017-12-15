package pages.TaskManager
import geb.Page

class TaskCreationPage extends Page{

    static at = {
        ctModalTitle.text() == "Create Task"
        ctModalSaveBtn.text().trim() == "Save"
        ctModalCancelBtn.text().trim() == "Cancel"
        ctModalCloseBtn
    }

    static content = {

        ctModalWindow(wait:true)    { $("div#editTaskPopup")}
        ctModalTitle                { $("span#ui-id-5", class:"ui-dialog-title ng-binding") }
        ctModalSaveBtn              { $("button#saveAndCloseBId")}
        ctModalCancelBtn            { $("button",class:"btn btn-default tablesave cancel")}
        ctModalNameTA(wait:true)    { $("#commentEditId")}
        ctModalEdited(wait:true)    { $("#commentTdId")}
        ctModalCloseBtn             { $("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}
        ctModalEventSelector        { $("select#moveEvent")}

    }



}
