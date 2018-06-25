package pages.Admin.MenuItems

import geb.Page
import modules.AdminModule

class ImportAccountsLandingPage extends Page{

    static at = {
        title == "Import Accounts"
        pageHeaderName.text() == "Import Accounts"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}