package pages.Tasks.Cookbook

import geb.Page

class TabHistoryTabActionsPage extends Page {

    static at = {
        hisTabActTab.parent(".active")
        hisTabActTabResetBtn.text() == "Reset"
        hisTabActTabRefreshBtn.text() == "Refresh"
        hisTabActTabDeleteBtn.text() == "Delete"
    }

    static content = {
        hisTabActTab                    { $("li", heading: "Actions").find("a")}
        hisTabActTabPublishBtn          { $("button", "ng-click": "tasks.publishUnpublishTaskBatch(tasks.selectedTaskBatch)")}
        hisTabActTabResetBtn            { $("button", "ng-click": "tasks.resetTaskBatch(tasks.selectedTaskBatch.id)")}
        hisTabActTabRefreshBtn          { $("button", "ng-click": "tasks.refreshTaskBatches()")}
        hisTabActTabDeleteBtn           { $("button", "ng-click": "tasks.deleteTaskBatch(tasks.selectedTaskBatch.id)")}
    }
}
