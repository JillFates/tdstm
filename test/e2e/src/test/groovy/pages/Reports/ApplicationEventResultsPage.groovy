package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class ApplicationEventResultsPage extends Page {

    static at = {
        applicationEventResultsPageTitle.text().trim() == "Application Event Results"
        applicationEventResultsPageBreadcrumbs[0].text() == "Reports"
        applicationEventResultsPageBreadcrumbs[1].text() == "Application Event Results"
    }

    static content = {
        applicationEventResultsPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        applicationEventResultsPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }


}