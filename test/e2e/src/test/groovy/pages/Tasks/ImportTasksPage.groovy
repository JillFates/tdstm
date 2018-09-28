package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksModule
import pages.Cookbook.*
import pages.Tasks.*

class ImportTasksPage extends Page {

    static at = {
        importTasksPageTitle.text().trim()  == "Import Tasks"
        importTasksPageBreadcrumbs[0].text()   == "Task"
        importTasksPageBreadcrumbs[1].text()   == "Import Tasks"

    }

    static content = {
        importTasksPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        importTasksPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        tasksModule { module TasksModule}
        commonsModule { module CommonsModule }
    }


}