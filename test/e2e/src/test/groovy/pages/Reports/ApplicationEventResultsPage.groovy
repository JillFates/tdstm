package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class ApplicationEventResultsPage extends Page {

    static at = {
        applicationEventResultsPageTitle.text().trim()  == "Application Event Results"
        applicationEventResultsPageBreadcrumbs[0].text()   == "Reports"

    }

    static content = {
        applicationEventResultsPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        applicationEventResultsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }


}