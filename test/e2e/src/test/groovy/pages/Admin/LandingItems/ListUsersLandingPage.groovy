package pages.Admin.LandingItems

import geb.Page
import modules.AdminModule

class ListUsersLandingPage extends Page{

    static at = {
        title == "User List - Active Users"
        pageHeaderName.text() == "UserLogin List - Active Users"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}
