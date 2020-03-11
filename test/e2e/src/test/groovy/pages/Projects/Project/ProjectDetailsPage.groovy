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
        pdProjectName {$("tbody").find("tr")[2].find("td")[1]}
        pdEditBtn { $("button", title:"Edit")}
        pdDeleteBtn { $("button", title:"Delete")}
        pdConfirmBtn (required: false){ $("button", title:"Confirm")}
        projectsModule { module ProjectsMenuModule}
        projectCloseIcon {$("tds-button-custom", class:"tds-generic-button").find(class:"action-button").find("button", tabindex:"0").find("clr-icon", shape:"close")}
        closeButton {$("tds-button-cancel")[0]}

    }

    def waitForProjectCreatedMessage(projName){
        waitFor {pdPageMessage.text().contains(projName + " was created")}
    }

    def clickOnDeleteButtonAndConfirm(){
        waitFor{pdDeleteBtn.click()}
        waitFor{confirmDelete()}
    }

    def clickEdit(){
        pdEditBtn.click()
    }

    def closeProjectModal(){
        projectCloseIcon.click()
    }

    def closeDetails(){
        closeButton.click()
    }

    def confirmDelete(){
        pdConfirmBtn.click()
    }
}
