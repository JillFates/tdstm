package pages.Reports

import geb.Page
import modules.CommonsModule
import modules.ReportsMenuModule

class TaskReportPage extends Page {

    static at = {
        taskReportPageTitle.text().trim()  == "Task Report"
        taskReportPageBreadcrumbs[0].text()   == "Report"
        taskReportPageBreadcrumbs[1].text()   == "Task Report"

    }

    static content = {
        taskReportPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        taskReportPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }


}