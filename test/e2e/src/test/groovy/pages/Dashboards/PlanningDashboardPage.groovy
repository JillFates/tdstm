package pages.Dashboards

import geb.Page

class PlanningDashboardPage extends Page {

    static at = {
        title == "Transition Planning Dashboard"
    }

    static content = {
        taskMenu { $("li.menu-parent-tasks").$("a")[0]}
        cookbookMenuItem { $("li.menu-parent-tasks-cookbook").$("a")[0]}
    }
}
