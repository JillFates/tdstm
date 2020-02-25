package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class ServerConflictsPage extends Page {

    static at = {
        serverConflictsPageTitle.text().trim()  == "Server Conflicts"
        serverConflictsPageBreadcrumbs[0].text()   == "Reports"
        serverConflictsPageBreadcrumbs[1].text()   == "Server Conflicts"

    }

    static content = {
        serverConflictsPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        serverConflictsPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }

}