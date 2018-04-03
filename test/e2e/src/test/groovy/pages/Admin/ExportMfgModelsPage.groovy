package pages.Admin

import geb.Page
import modules.AdminModule

class ExportMfgModelsPage extends Page{

    static at = {
        title == "Sync Management"
        pageHeaderName.text() == "Export Mfg & Models"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}
