package pages.Admin

import geb.Page
import modules.AdminModule

class AdminPortalPage extends Page {

    static at = {
        title == "TDS TransitionManager™ Admin Portal"
        pageHeaderName.text() == "Admin Portal"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }
}
