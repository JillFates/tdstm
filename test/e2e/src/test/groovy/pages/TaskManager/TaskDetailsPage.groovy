package pages.TaskManager
import geb.Page

class TaskDetailsPage extends Page{

    static at = {
        tdModalTitle.text() == "Task Details"
        tdModalEditBtn.text().trim() == "Edit"
        tdModalDeleteBtn.text().trim() == "Delete"
        tdModalCloseBtn
    }

    static content = {

        tdModalWindow(wait:true)    { $("div#showTaskPopup")}
        tdModalTitle                { tdModalWindow.find("span#ui-id-5", class:"ui-dialog-title") }
        tdModalEditBtn              { $("button","ng-click":"editComment();")}
        tdModalDeleteBtn            { $("button","ng-click":"deleteComment()")}
        tdModalCloseBtn             { $("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}
        tdModalTaskName(wait:true)  { $("#commentTdId")}
        tdModalEventSelector        { $("select#moveEvent")}

    }



}
