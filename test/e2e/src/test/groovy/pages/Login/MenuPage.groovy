package pages.Login

import geb.Page
import modules.AdminModule
import modules.MenuModule
import modules.ProjectsMenuModule
import modules.AssetsMenuModule
import modules.TasksMenuModule
import modules.ReportsMenuModule
import modules.CommonsModule

import modules.PlanningMenuModule

class MenuPage extends Page {

    static at = {
        waitFor(30){menuModule.displayed}
    }

    static content = {
        menuModule (wait: true){ module MenuModule}
        adminModule (wait: true) { module AdminModule}
        projectsModule (wait: true){ module ProjectsMenuModule}
        assetsModule { module AssetsMenuModule}
        tasksModule { module TasksMenuModule}
        planningModule { module PlanningMenuModule}
        reportsModule { module ReportsMenuModule}
        commonsModule { module CommonsModule }
    }
}
