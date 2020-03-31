package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class ApplicationProfilesPage extends Page {

    static at = {
        applicationProfilesPageTitle.text().trim()  == "Application Profiles"
        applicationProfilesPageBreadcrumbs[0].text()   == "Reports"
        applicationProfilesPageBreadcrumbs[1].text()   == "Application Profiles"

    }

    static content = {
        applicationProfilesPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        applicationProfilesPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }


}