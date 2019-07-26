package pages.Projects.Project

import geb.Page
import modules.MenuModule
import modules.ProjectsMenuModule

class ProjectEditPage extends Page {

    static at = {
        pdHeaderTitle.text().trim() == "Edit Project"

    }

    static content = {
        pdHeaderTitle { $("section", class: "content-header").find("h1")}
        pjctName { $("input#name") }
        pjctDescription { $("textarea#description") }
        pjctSartDate { $("input#startDateId") }
        pjctCompletionDate { $("input#completionDateId") }
        updateBtn { $("input.save") }
        cancelBtn { $("input.cancel") }
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