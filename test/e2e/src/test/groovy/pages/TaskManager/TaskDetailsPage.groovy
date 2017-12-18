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

        tdModalWindow(wait:true)        { $("div#showTaskPopup")}
        tdModalTitle                    { tdModalWindow.find("span#ui-id-5", class:"ui-dialog-title") }
        tdModalTaskName(wait:true)      { tdModalWindow.find("#commentTdId")}
        tdModalEventSelector            { tdModalWindow.find("select#moveEvent")}
        tdModalStatusSelector(wait:true){ tdModalWindow.find("select#status")}
        tdModalEditBtn                  { tdModalWindow.find("button","ng-click":"editComment();")}
        tdModalDeleteBtn                { tdModalWindow.find("button","ng-click":"deleteComment()")}
        tdModalCloseBtn                 { tdModalWindow.find("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}

    }



}
