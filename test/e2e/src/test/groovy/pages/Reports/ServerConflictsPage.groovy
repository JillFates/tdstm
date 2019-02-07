package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class ServerConflictsPage extends Page {

    static at = {
        serverConflictsPageTitle.text().trim()  == "Server Conflicts"
        serverConflictsPageBreadcrumbs[0].text()   == "Reports"
        serverConflictsPageBreadcrumbs[1].text()   == "Server"

    }

    static content = {
        serverConflictsPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        serverConflictsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }

}