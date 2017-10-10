package pages.Dashboards

import geb.Page

class UserDashboardPage extends Page {

    static at = {
        title.contains("User Dashboard For")
    }

    static content = {
        taskMenu { $("li.menu-parent-tasks").$("a")[0] }
        cookbookMenuItem { $("li.menu-parent-tasks-cookbook").$("a")[0] }
    }
}
