package pages.Projects

import geb.Page
import modules.ProjectsModule

class ActionsPage extends Page {

    static at = {
        modaltitle.text() == "Actions"
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")}
        projectsModule { module ProjectsModule}
        createActionBtn {$("button", class:"btn btn-default" , id:"btnCreate")}
    }


}
