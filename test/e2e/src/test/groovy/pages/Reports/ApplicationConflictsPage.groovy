package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsModule

class ApplicationConflictsPage extends Page {

    static at = {
        applicationConflictsPageTitle.text().trim()  == "Application Conflicts"
        applicationConflictsPageBreadcrumbs[0].text()   == "Reports"
        applicationConflictsPageBreadcrumbs[1].text()   == "Application"

    }

    static content = {
        applicationConflictsPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        applicationConflictsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        reportsModule { module ReportsModule}
        commonsModule { module CommonsModule }
    }


}