package pages.Admin

import geb.Page
import modules.AdminModule

class ListModelsPage extends Page{

    static at = {
        title == "Model List"
        pageHeaderName.text() == "Model List"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}
