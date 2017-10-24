package pages.TaskManager
import geb.Page

class CreateTaskPage extends Page{

    static at = {
        //ctModalTitle.text() == "Create Task"
        //ctModalNameTA.text() == ""
        //ctModalSaveBt.text() == "Save"
        modalXbtn
    }

    static content = {

        ctPopUp(wait:true)          { $("editTaskPopup")}
        //ctModalTitle              {$("#ui-id-5").find("span", class:"ui-dialog-title ng-binding") }
        ctModalTitle                { $("span" , "class":"ui-dialog-title ng-binding")}
        ctModalSaveBt               { $("#saveAndCloseBId")}
        ctModalCancelBt             { $("div",class:"btn btn-default tablesave cancel")}
        ctModalNameTA(wait:true)    { $("#commentEditId")}
        ctModalEdited(wait:true)    { $ "#commentTdId"}
        //firstElementTaskTbl         { $("#taskListIdGrid").$("tr")[1].$("td")[0].find("a")}
        //deletebtn                    { $("button" , "ng-click":"deleteComment()")}
        //deletebtn                   { $("button" , "ng-click":"deleteComment()")}
        //deletebtn                    { $("div" , "class":"buttons").$("div")[0].$("button")[1].find("ng-click")}
        deletebtn                    { $("span" , "class":"glyphicon glyphicon-minus")}
        modalXbtn                       {$("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}
        //contextSelector             { $("select#contextSelector2") }
        //taskTable                   { $("taskListIdGrid")}


    }



}
