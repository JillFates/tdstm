package pages.Projects.Project

import geb.Page
import modules.MenuModule
import modules.ProjectsMenuModule

class ProjectDetailsPage extends Page {

    static at = {
        pdHeaderTitle.text().trim() == "Project Details"
        // TODO project table have no references to its items
        pdEditBtn.value() == "Edit"
        pdDeleteBtn.value() == "Delete"
        pdFieldSetBtn.value() == "Field Settings"
    }

    static content = {
        pdHeaderTitle { $("section", class:"content-header").find("h1")}
        pdProjDetTable { $("table",class:"show-project-table")}
        pdPageMessage (required: false, wait:true) { $("div", class:"message")}  //TODO add ID reference to message div
        // TODO project table have no references to its items
        pdProjectName {$("#updateShow").find("td.valueNW")[2]}
        pdEditBtn { $("input", type:"submit",class:"edit", name:"_action_Edit")}
        pdDeleteBtn { $("input", type:"submit",class:"delete", name:"_action_Delete")}
        pdFieldSetBtn { $("input", type:"button",class:"show")}
        projectsModule { module ProjectsMenuModule}
    }

    def waitForProjectCreatedMessage(projName){
        waitFor {pdPageMessage.text().contains(projName + " was created")}
    }

    def clickOnDeleteButtonAndConfirm(){
        withConfirm(true){waitFor{pdDeleteBtn.click()}}
    }

    def clickEdit(){
        pdEditBtn.click()
    }
}
