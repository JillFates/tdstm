package pages.Admin

import geb.Page
import modules.AdminModule

class ListManufacturersPage extends Page{

    static at = {
        title == "Manufacturer List"
        pageHeaderName.text() == "Manufacturer List"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}
