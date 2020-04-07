package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class ApplicationConflictsPage extends Page {

    static at = {
        applicationConflictsPageTitle.text().trim()  == "Application Conflicts"
        applicationConflictsPageBreadcrumbs[0].text()   == "Reports"
        applicationConflictsPageBreadcrumbs[1].text()   == "Application Conflicts"

    }

    static content = {
        applicationConflictsPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        applicationConflictsPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }


}