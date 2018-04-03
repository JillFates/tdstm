package pages.Admin

import geb.Page
import modules.AdminModule


class ListCompaniesAdminPage extends Page{

    static at = {
        title == "Company List"
        pageHeaderName.text() == "Company List"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}
