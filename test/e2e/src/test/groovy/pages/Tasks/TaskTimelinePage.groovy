package pages.Tasks

import geb.Page
import modules.CommonsModule
import modules.TasksMenuModule
import pages.Tasks.Cookbook.CookbookPage
import pages.Tasks.*

class TaskTimelinePage extends Page {

    static at = {
        taskTimelinePageTitle.text().trim()  == "Task Timeline"
        taskTimelinePageBreadcrumbs[0].text()   == "Task"
        taskTimelinePageBreadcrumbs[1].text()   == "Timeline"

    }

    static content = {
        taskTimelinePageTitle (wait:true) { $("section", class:"content-header").find("h2")}
        taskTimelinePageBreadcrumbs { $("ol", class:"legacy-breadcrumb").find("li a")}
        tasksModule { module TasksMenuModule}
        commonsModule { module CommonsModule }
    }


}