package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class TaskReportPage extends Page {

    static at = {
        taskReportPageTitle.text().trim()  == "Task Report"
        taskReportPageBreadcrumbs[0].text()   == "Reports"
        taskReportPageBreadcrumbs[1].text()   == "Task Report"

    }

    static content = {
        taskReportPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        taskReportPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }


}