package pages.Projects

import geb.Page

class PojectDetailsPage extends Page {

    static at = { title == "AdminPortalPage" }

    static content = {
        taskMenu { $("li.menu-parent-tasks").$("a")[0] }
        cookbookMenuItem { $("li.menu-parent-tasks-cookbook").$("a")[0] }
    }
}
