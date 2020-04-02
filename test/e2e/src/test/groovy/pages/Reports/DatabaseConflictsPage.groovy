package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class DatabaseConflictsPage extends Page {

    static at = {
        databaseConflictsPageTitle.text().trim()  == "Database Conflicts"
        databaseConflictsPageBreadcrumbs[0].text()   == "Reports"
        databaseConflictsPageBreadcrumbs[1].text()   == "Database Conflicts"

    }

    static content = {
        databaseConflictsPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        databaseConflictsPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }


}