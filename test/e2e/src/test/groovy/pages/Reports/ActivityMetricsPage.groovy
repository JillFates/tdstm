package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class ActivityMetricsPage extends Page {

    static at = {
        activityMetricsPageTitle.text().trim()  == "Activity Metrics Report"
        activityMetricsPageBreadcrumbs[0].text()   == "Reports"
        activityMetricsPageBreadcrumbs[1].text()   == "Activity Metrics"

    }

    static content = {
        activityMetricsPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        activityMetricsPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }


}