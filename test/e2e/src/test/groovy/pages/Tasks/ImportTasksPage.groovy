package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksMenuModule
import pages.Cookbook.*
import pages.Tasks.*

class ImportTasksPage extends Page {

    static at = {
        importTasksPageTitle.text().trim()  == "Import Tasks"
        importTasksPageBreadcrumbs[0].text()   == "Task"
        importTasksPageBreadcrumbs[1].text()   == "Import Tasks"
    }

    static content = {
        importTasksPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        importTasksPageBreadcrumbs { $("ol", class:"legacy-breadcrumb").find("li a")}
        tasksModule { module TasksMenuModule}
        commonsModule { module CommonsModule }
    }
}