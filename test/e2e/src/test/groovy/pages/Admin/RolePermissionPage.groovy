package pages.Admin

import geb.Page
import modules.AdminModule

class RolePermissionPage extends Page{

    static at = {
        title == "Show Role Permissions"
        pageHeaderName.text() == "Role Permissions"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }


}
