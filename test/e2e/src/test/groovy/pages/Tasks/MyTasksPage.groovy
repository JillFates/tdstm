package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksMenuModule
import pages.Tasks.*

class MyTasksPage extends Page {

    static at = {
        myTasksPageTitle.text().trim()  == "My Task"
        myTasksPageBreadcrumbs[0].text()   == "Tasks"
        myTasksPageBreadcrumbs[1].text()   == "My Task"

    }

    static content = {
        myTasksPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        myTasksPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        tasksModule { module TasksMenuModule}
        commonsModule { module CommonsModule }
    }


}