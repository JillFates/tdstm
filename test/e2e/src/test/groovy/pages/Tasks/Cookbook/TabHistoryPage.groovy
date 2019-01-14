package pages.Tasks.Cookbook

import geb.Page

class TabHistoryPage extends Page {

    static at = {
        historyTab.parent(".active")
        historyTab.text() == "History"
        hisTabActTab.text() == "Actions"
        hisTabTasksTab.text() == "Tasks"
        hisTabGenLogTab.text() == "Generation Log"
    }

    static content = {
        historyTab(wait:true)           { $("li", heading: "History").find("a")}
        hisTabActTab                    { $("li", heading: "Actions").find("a")}
        hisTabTasksTab                  { $("li", heading: "Tasks").find("a")}
        hisTabGenLogTab                 { $("li", heading: "Generation Log").find("a")}
        hisTabBatchGrid                 { $("div", "ng-grid":"tasks.tasksGrid")}
        hisTabBatchGridHead             { hisTabBatchGrid.find("div", class:"ngHeaderContainer")}
        hisTabBatchGridHeadCols         { hisTabBatchGridHead.find("div", "ng-repeat":"col in renderedColumns")}
        hisTabBatchGridRows             { hisTabBatchGrid.find("div", "ng-repeat":"row in renderedRows")}
        hisTabBatchGridRowsCols         { hisTabBatchGridRows.find("div", "ng-repeat":"col in renderedColumns")}
    }
}
