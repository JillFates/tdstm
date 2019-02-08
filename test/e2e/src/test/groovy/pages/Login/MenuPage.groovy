package pages.Login

import geb.Page
import modules.AdminModule
import modules.MenuModule
import modules.ProjectsMenuModule
import modules.AssetsMenuModule
import modules.TasksMenuModule
import modules.ReportsMenuModule

import modules.PlanningMenuModule

class MenuPage extends Page {

    static at = {
        contextPath.value() == "/tdstm"
    }

    static content = {
        contextPath { $("input",id:"contextPath")}
        menuModule { module MenuModule}
        adminModule { module AdminModule}
        projectsModule { module ProjectsMenuModule}
        assetsModule { module AssetsMenuModule}
        tasksModule { module TasksMenuModule}
        planningModule { module PlanningMenuModule}
        reportsModule { module ReportsMenuModule}
    }
}
