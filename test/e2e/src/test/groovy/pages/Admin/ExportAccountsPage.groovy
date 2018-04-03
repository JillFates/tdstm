package pages.Admin

import geb.Page
import modules.AdminModule


class ExportAccountsPage extends Page {


    static at = {
        title == "Export Accounts"
        pageHeaderName.text() == "Export Accounts"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }


}
