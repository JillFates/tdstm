package pages.Admin

import geb.Page

class AdminPortalPage extends Page {

    static at = {
        title == "TDS TransitionManager™ Admin Portal"
    }

    static content = {
        taskMenu { $("li.menu-parent-tasks").$("a")[0] }
        cookbookMenuItem { $("li.menu-parent-tasks-cookbook").$("a")[0] }
    }
}
