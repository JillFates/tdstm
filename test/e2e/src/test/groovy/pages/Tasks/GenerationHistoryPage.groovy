package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksModule
import pages.Cookbook.*
import pages.Tasks.*

class GenerationHistoryPage extends Page {

    static at = {
        generationHistoryPageTitle.text().trim()  == "Generation History"
        generationHistoryPageBreadcrumbs[0].text()   == "Tasks"
    }

    static content = {
        generationHistoryPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        generationHistoryPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        tasksModule { module TasksModule}
        commonsModule { module CommonsModule }
    }


}