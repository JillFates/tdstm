package pages.Admin.LandingItems

import geb.Page
import modules.AdminModule

class LicenseLandingPage extends Page{

    static at = {
        title == "Administer Licenses"
        pageHeaderName.text() == "Administer Licenses"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header ng-scope").find("h1")}
    }

}
