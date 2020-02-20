package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksMenuModule
import pages.Tasks.*

class GoJSTaskGraphPage extends Page {

    static at = {
        taskGraphPageTitle.text().trim()  == "Task Graph"
        taskGraphPageBreadcrumbs[0].text()   == "Task"
        taskGraphPageBreadcrumbs[1].text()   == "GoJS Task Graph"

    }

    static content = {
        taskGraphPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        taskGraphPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        tasksModule { module TasksMenuModule}
        commonsModule { module CommonsModule }
    }


}