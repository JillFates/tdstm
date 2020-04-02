package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksMenuModule
import pages.Tasks.*

class TaskGraphPage extends Page {

    static at = {
        taskGraphPageTitle.text().trim()  == "Task Graph"
        taskGraphPageBreadcrumbs[0].text()   == "Task"
        taskGraphPageBreadcrumbs[1].text()   == "Graph"

    }

    static content = {
        taskGraphPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        taskGraphPageBreadcrumbs { $("ol", class:"legacy-breadcrumb").find("li a")}
        tasksModule { module TasksMenuModule}
        commonsModule { module CommonsModule }
    }


}