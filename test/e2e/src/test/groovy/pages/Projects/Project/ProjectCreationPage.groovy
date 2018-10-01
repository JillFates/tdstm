package pages.Projects.Project

import geb.Page

class ProjectCreationPage extends Page{

    static at = {
        pcPageTitle.text().trim() == "Create Project"
        pcSaveBtn.value() == "Save"  //TODO Save Button has no ID
        pcCancelBtn.value() == "Cancel"  //TODO Cancel Button has no ID
    }

    static content = {
        pcPageTitle { $("section", class:"content-header").find("h1")}
        pcPageMessage (required: false, wait:true) { $("div", class:"message")}
        pcPageForm { $("form", id:"createProjectForm")}
        pcClientSelector { pcPageForm.find("div#s2id_clientId")}
        pcClientItem { $("li.select2-results-dept-0:nth-child(1)")}
        pcProjectCode { pcPageForm.find("input#projectCode")}
        pcProjectName { pcPageForm.find("input#name")}
        pcProjTypeSelector { pcPageForm.find("div#s2id_projectType")}
        pcDescription { pcPageForm.find("textarea#description")}
        pcComment { pcPageForm.find("textarea#comment")}
        pcStartDate { pcPageForm.find("input#startDateId")}
        pcCompletionDate { pcPageForm.find("input#completionDateId")}
        pcPartner { pcPageForm.find("input", tyype:"button", value:"Add Partner")}
        pcProjectLogoFile { pcPageForm.find("input#projectLogo")}
        pcDefaultBundle { pcPageForm.find("input#defaultBundle")}
        pcProjManSelector { pcPageForm.find("div#s2id_projectManagerId")}
        pcTimeZone { pcPageForm.find("input#timezone")}
        pcChangeTimeZoneBtn { pcPageForm.find("input", value:"Change")} //TODO Cahnge Button has no ID
        pcworkflowSelector { pcPageForm.find("div#s2id_workflowCode")}
        pcSaveBtn { $("input", class:"save", value:"Save")} //TODO Save Button has no ID
        pcCancelBtn { $("input", type:"button", class:"cancel", value:"Cancel")} //TODO Cancel Button has no ID
        // pcPlanMethod        {} //TODO Plan Methodology label was wrong 'for='. Also value has no reference
    }

    def fillInFields(projInfoMap){
        waitFor {pcClientSelector.click()}
        waitFor {pcClientItem.click()}
        pcProjectCode = projInfoMap.projName
        pcProjectName = projInfoMap.projName
        pcDescription = projInfoMap.projDesc
        pcComment = projInfoMap.projComment
        pcCompletionDate = projInfoMap.projCompDate
    }

    def clickOnSaveButton(){
        waitFor {pcSaveBtn.click()}
    }
}