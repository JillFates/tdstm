package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksMenuModule
import pages.Cookbook.*
import pages.Tasks.*

class GenerationHistoryPage extends Page {

    static at = {
        generationHistoryPageTitle.text().trim()  == "Cookbook"
        generationHistoryPageBreadcrumbs[0].text()   == "Tasks"
        generationHistoryPageBreadcrumbs[1].text()   == "Cookbook"
    }

    static content = {
        generationHistoryPageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        generationHistoryPageBreadcrumbs { $("ol", class:"legacy-breadcrumb").find("li a")}
        tasksModule { module TasksMenuModule}
        commonsModule { module CommonsModule }
    }


}