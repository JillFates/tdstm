package pages.Dashboards

import geb.Page
import modules.MenuModule

class UserDashboardPage extends Page {

    static at = {
        title.contains("User Dashboard For")
    }

    static content = {
        taskMenu { $("li.menu-parent-tasks").$("a")[0]}
        cookbookMenuItem { $("li.menu-parent-tasks-cookbook").$("a")[0]}
        taskManagerMenuItem { $("li.menu-parent-tasks-task-manager").$("a")[0]}
    }
}
