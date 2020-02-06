package pages.Projects.Project

import geb.Page
import modules.MenuModule
import modules.ProjectsMenuModule

class ProjectDetailsPage extends Page {

    static at = {
        pdHeaderTitle.text() == "Project Detail"
        // TODO project table have no references to its items
        pdEditBtn.displayed
        pdDeleteBtn.displayed
    }

    static content = {
        pdHeaderTitle { $( class:"modal-title-container").find(class:"modal-title")}
        pdProjDetTable { $("table",class:"show-project-table")}
        pdPageMessage (required: false, wait:true) { $("div", class:"message")}  //TODO add ID reference to message div
        // TODO project table have no references to its items
        pdProjectName {$("#updateShow").find("td.valueNW")[2]}
        pdEditBtn { $(class:"modal-sidenav").find("tds-button-edit").find("button", title:"Edit")}
        pdDeleteBtn { $(class:"modal-sidenav").find("tds-button-delete").find("button", title:"Delete")}
        projectsModule { module ProjectsMenuModule}
        projectCloseIcon {$("tds-button-custom", class:"tds-generic-button").find(class:"action-button").find("button", tabindex:"0").find("clr-icon", shape:"close")}
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

    def closeProjectModal(){
        projectCloseIcon.click()
    }
}
