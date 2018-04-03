package pages.Admin

import geb.Page
import modules.AdminModule

class ListUsersAdminPage extends Page{

    static at = {
        title == "User List - Active Users"
        pageHeaderName.text() == "UserLogin List - Active Users"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}
