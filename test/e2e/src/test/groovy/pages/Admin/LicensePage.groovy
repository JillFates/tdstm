package pages.Admin

import geb.Page
import modules.AdminModule

class LicensePage extends Page{

    static at = {
        title == "License Admin"
        pageHeaderName.text() == "License Admin"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h2")}
    }

}
