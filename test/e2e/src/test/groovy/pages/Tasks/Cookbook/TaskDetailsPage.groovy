package pages.Tasks.Cookbook

import geb.Page

class TaskDetailsPage extends Page {

    // TODO The TaskDetailsPage from Task Manager should be used

    static at = {
        taskDetailsModalWindow.isDisplayed()
        taskDetailsModalEditBtn //TODO check if enabled
        taskDetailsModalEditBtn.text() == "Edit"
        taskDetailsModalDeleteBtn //TODO check if enabled
        taskDetailsModalDeleteBtn.text() == "Delete"
    }

    static content = {

        loadingIndicator(wait:true)             { $("div", "ng-show": "isLoading")}
        taskDetailsModalWindow(wait:true)       { $("div", "window-class":"modal-task")}
        taskDetailsNumberLabel(wait:true)       { $("label", for:"contextSelector2").text()}
        taskDetailsNumber                       { $("span#taskNumberId").find("b",class:"ng-binding")}
        taskDetailsTaskName                     { $("textarea#commentTdId")}
        taskDetailsModalCloseBtn(wait:true)     { $("button", "ng-click": "close()")}
        taskDetailsModalEditBtn(wait:true)      { $("button", "ng-click": "editComment();")}
        taskDetailsModalDeleteBtn(wait:true)    { $("button", "ng-click": "deleteComment()")}
    }
}
