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
        tdModalTaskPerson               { tdModalWindow.find("span#assignedToId")}
        tdModalTaskTeam                 { tdModalWindow.find("span#roleTdId")}
        tdModalTaskEvent(wait:true)     { tdModalWindow.find("td#eventName")}
        tdModalTaskAssetName            { tdModalWindow.find("td#assetShowValueId")}
        tdModalTaskStatus(wait:true)    { tdModalWindow.find("td#statusShowId")}
        tdModalAddPredecessor(wait:true){ tdModalWindow.find("a#createSucTask")}
        tdModalAddSuccessor(wait:true)  { tdModalWindow.find("a#createPredTask")}
        tdModalEditBtn                  { tdModalWindow.find("button","ng-click":"editComment();")}
        tdModalDeleteBtn                { tdModalWindow.find("button","ng-click":"deleteComment()")}
        tdModalCloseBtn                 { tdModalWindow.find("button", "class":"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close")}

    }



}
