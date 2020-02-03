package pages.Projects

import geb.Page
import modules.ProjectsMenuModule

class ActionsPage extends Page {

    static at = {
        modaltitle.text() == "Actions"
        pageBreadcrumbs[0].text() == "Project"
        pageBreadcrumbs[1].text() == "Actions"
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h2")}
        projectsModule { module ProjectsMenuModule}
        createActionBtn {$("button", class:"btn btn-default" , id:"btnCreate")}
        pageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
    }


}
