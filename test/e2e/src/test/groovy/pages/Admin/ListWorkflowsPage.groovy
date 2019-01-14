package pages.Admin

import geb.Page
import modules.AdminModule

class ListWorkflowsPage extends Page{

    static at = {
        title == "Workflows"
        pageHeaderName.text() == "Workflows"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
    }

}
