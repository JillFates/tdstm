package pages.Admin.LandingItems

import geb.Page
import modules.AdminModule

class ListStaffLandingPage extends Page{

    static at = {
        title == "Staff List"
        pageHeaderName.text() == "Staff List"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}