package pages.Admin.MenuItems

import geb.Page
import modules.AdminModule

class ExportMfgModelsLandingPage extends Page{

    static at = {
        title == "Sync Management"
        pageHeaderName.text() == "Export Mfg & Models"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}
