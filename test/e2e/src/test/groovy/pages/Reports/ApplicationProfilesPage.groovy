package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsModule

class ApplicationProfilesPage extends Page {

    static at = {
        applicationProfilesPageTitle.text().trim()  == "Application Profiles"
        applicationProfilesPageBreadcrumbs[0].text()   == "Reports"
        applicationProfilesPageBreadcrumbs[1].text()   == "Profiles"

    }

    static content = {
        applicationProfilesPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        applicationProfilesPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        reportsModule { module ReportsModule}
        commonsModule { module CommonsModule }
    }


}