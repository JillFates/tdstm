package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsModule

class ActivityMetricsPage extends Page {

    static at = {
        activityMetricsPageTitle.text().trim()  == "Activity Metrics Report"
        activityMetricsPageBreadcrumbs[0].text()   == "Reports"
        activityMetricsPageBreadcrumbs[1].text()   == "Activity"

    }

    static content = {
        activityMetricsPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        activityMetricsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        reportsModule { module ReportsModule}
        commonsModule { module CommonsModule }
    }


}