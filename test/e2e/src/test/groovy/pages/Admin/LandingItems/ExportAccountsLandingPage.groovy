package pages.Admin.LandingItems

import geb.Page
import modules.AdminModule

class ExportAccountsLandingPage extends Page{

    static at = {
        title == "Export Accounts"
        pageHeaderName.text() == "Export Accounts"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}