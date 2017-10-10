package pages.Cookbook

import geb.Page

class TabTaskGenTabSummaryPage extends Page {

    static at = {
        tskGTabSummaryTab.parent(".active")
    }

    static content = {
        tskGTabSummaryTab             { $("li", heading: "Summary").find("a") }
        tskGTabSummaryList            { $("div", "ui-view": "taskBatchCompleted").find("ul", class:"summaryList ng-scope")}
    }
}
