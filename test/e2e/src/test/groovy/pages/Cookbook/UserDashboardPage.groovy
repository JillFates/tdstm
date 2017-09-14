package pages.Cookbook

import geb.Page

class UserDashboardPage extends Page {

    static at = { title == "User Dashboard For e2e test user" }

    static content = {
        //taskMenu { $("li.menu-parent-tasks").$("a")[0] }
        taskMenu { $("li.menu-parent-tasks").$("a")[0] }
        cookbookMenuItem { $("li.menu-parent-tasks-cookbook").$("a")[0] }
    }
}
