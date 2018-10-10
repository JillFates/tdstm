package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksModule
import pages.Tasks.*

class TaskGraphPage extends Page {

    static at = {
        taskGraphPageTitle.text().trim()  == "Task Graph"
        taskGraphPageBreadcrumbs[0].text()   == "Task"
        taskGraphPageBreadcrumbs[1].text()   == "Graph"

    }

    static content = {
        taskGraphPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        taskGraphPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        tasksModule { module TasksModule}
        commonsModule { module CommonsModule }
    }


}