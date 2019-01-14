package pages.Admin

import geb.Page
import modules.AdminModule

class AssetOptionsPage extends Page {

    static at = {
        title == "Asset Options"
        pageHeaderName.text() == "Administrative Setting"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }
}
