package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsModule

class DatabaseConflictsPage extends Page {

    static at = {
        databaseConflictsPageTitle.text().trim()  == "Database Conflicts"
        databaseConflictsPageBreadcrumbs[0].text()   == "Reports"
        databaseConflictsPageBreadcrumbs[1].text()   == "Database"

    }

    static content = {
        databaseConflictsPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        databaseConflictsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        reportsModule { module ReportsModule}
        commonsModule { module CommonsModule }
    }


}