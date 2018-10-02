package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksModule
import pages.Tasks.Cookbook.CookbookPage
import pages.Tasks.*

class TaskTimelinePage extends Page {

    static at = {
        taskTimelinePageTitle.text().trim()  == "Task Timeline"
        taskTimelinePageBreadcrumbs[0].text()   == "Task"
        taskTimelinePageBreadcrumbs[1].text()   == "Timeline"

    }

    static content = {
        taskTimelinePageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        taskTimelinePageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        tasksModule { module TasksModule}
        commonsModule { module CommonsModule }
    }


}