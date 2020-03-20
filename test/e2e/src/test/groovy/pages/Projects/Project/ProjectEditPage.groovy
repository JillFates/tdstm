package pages.Projects.Project

import geb.Page
import modules.MenuModule
import modules.ProjectsMenuModule

class ProjectEditPage extends Page {

    static at = {
        pdHeaderTitle.text().trim() == "Project Edit"

    }

    static content = {
        pdHeaderTitle { $("div", class: "modal-title")}
        pjctName { $("input#projectName") }
        pjctCode {$("input#projectCode")}
        pjctDescription { $("textarea#description") }
        pjctSartDate { $("input#startDateId") }
        //pjctCompletionDate { $("input#completionDateId") }
        updateBtn { $("button", title: "Save") }
        cancelBtn { $("button", title: "Cancel") }
        deleteBtn { $("button", title: "Delete") }
        projectsModule { module ProjectsMenuModule }

    }


    def clickUpdate() {
        updateBtn.click()
    }

    def clickCancel() {
        cancelBtn.click()
    }

    def enterProjectName(text) {
        pjctName = text
    }

    def editProjectName() {
        pjctName = pjctName.value() + " Edited"
    }

    def enterProjectDescription(text) {
        pjctDescription = text
    }

    def editProjectDescription() {
        pjctDescription = pjctDescription.value() + " Edited"
    }

    def editStartDate(text) {
        pjctSartDate.value = text
    }

    /**
    * Enters a date 6 months in the future*
    */
    def editCompletionDate(dt) {
        pjctCompletionDate = dt
    }

}