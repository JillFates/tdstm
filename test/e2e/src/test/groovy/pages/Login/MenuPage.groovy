package pages.Login

import geb.Page
import modules.AdminModule
import modules.MenuModule
import modules.ProjectsModule
import modules.AssetsModule

import modules.PlanningModule

class MenuPage extends Page {

    static at = {
        contextPath.value() == "/tdstm"
    }

    static content = {
        contextPath { $("input",id:"contextPath")}
        menuModule { module MenuModule}
        adminModule { module AdminModule}
        projectsModule { module ProjectsModule}
        assetsModule { module AssetsModule}

        planningModule { module PlanningModule}
    }
}
