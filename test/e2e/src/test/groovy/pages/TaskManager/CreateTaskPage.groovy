package pages.TaskManager
import geb.Page

class CreateTaskPage extends Page{

    static at = {
        //ctModalTitle.text() == "Create Task"
        //ctModalNameTA.text() == ""
        ctModalSaveBt.text() == "Save"
    }

    static content = {

        ctPopUp(wait:true)          { $("editTaskPopup")}
        //ctModalTitle              {$("#ui-id-5").find("span", class:"ui-dialog-title ng-binding") }
        ctModalTitle                { $("span" , "class":"ui-dialog-title ng-binding")}
        ctModalSaveBt               { $("#saveAndCloseBId")}
        ctModalCancelBt             { $("div",class:"btn btn-default tablesave cancel")}
        ctModalNameTA               { $("#commentEditId")}
        //contextSelector             { $("select#contextSelector2") }
        //taskTable                   { $("taskListIdGrid")}


    }



}
