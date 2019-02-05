package pages.Projects

import geb.Page
import modules.ProjectsMenuModule

class ActionsPage extends Page {

    static at = {
        modaltitle.text() == "API Actions"
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")}
        projectsModule { module ProjectsMenuModule}
        createActionBtn {$("button", class:"btn btn-default" , id:"btnCreate")}
    }


}
