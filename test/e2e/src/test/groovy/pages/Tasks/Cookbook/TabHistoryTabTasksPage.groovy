package pages.Tasks.Cookbook

import geb.Page

class TabHistoryTabTasksPage extends Page {

    static at = {
        hisTabTasksTab.text() == "Tasks"
        hisTabTasksTab.parent(".active")
    }

    static content = {
        hisTabTasksTab                           { $("li", heading: "Tasks").find("a")}
        hisTabTasksTabTasksGrid (wait:true)      { $("div", "ng-grid":"assetComments.tasksGrid")}
        hisTabTasksTabTasksGridHead              { hisTabTasksTabTasksGrid.find("div", class:"ngHeaderContainer")}
        hisTabTasksTabTasksGridHeadCols          { hisTabTasksTabTasksGridHead.find("div", class:"ngHeaderText")}
        hisTabTasksTabTasksList(required: false) { hisTabTasksTabTasksGrid.find("div", "ng-repeat":"row in renderedRows")}
        hisTabTasksTabTasksFirstRowValues        { hisTabTasksTabTasksList.getAt(0).find("div", "ng-repeat":"col in renderedColumns")}
    }
}
