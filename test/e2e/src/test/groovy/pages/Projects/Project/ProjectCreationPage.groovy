package pages.Projects.Project

import geb.Page
import org.openqa.selenium.Keys

class ProjectCreationPage extends Page{

    static at = {
        pcPageTitle.text().trim() == "Project Create"
    }

    static content = {
        pcPageTitle (wait: true){ $("div", class:"modal-title")}
        pcPageMessage (required: false, wait:true) { $("div", class:"message")}
        pcPageForm (wait: true){ $("div", class:"tds-modal-content")}
        pcClientSelector { $("kendo-dropdownlist#client")}
        pcClientItem (required: false) { $("li.k-item.ng-star-inserted:nth-child(1)")}
        pcProjectCode { pcPageForm.find("input#projectCode")}
        pcProjectName { pcPageForm.find("input#projectName")}
        pcProjTypeSelector { pcPageForm.find("kendo-dropdownlist#projectType")}
        pcDescription { pcPageForm.find("textarea#description")}
        pcComment { pcPageForm.find("textarea#comment")}
        pcStartDate { pcPageForm.find("input#startDateId")}
        pcCompletionDate { pcPageForm.find("kendo-dateinput.k-widget.k-dateinput").find("input")}
        pcPartner { pcPageForm.find("input", tyype:"button", value:"Add Partner")}
        pcProjectLogoFile { pcPageForm.find("input#projectLogo")}
        pcDefaultBundle { pcPageForm.find("input#defaultBundle")}
        pcProjManSelector { pcPageForm.find("div#s2id_projectManagerId")}
        pcTimeZone { pcPageForm.find("input#timezone")}
        pcChangeTimeZoneBtn { pcPageForm.find("input", value:"Change")} //TODO Cahnge Button has no ID
        pcworkflowSelector { pcPageForm.find("div#s2id_workflowCode")}
        pcSaveBtn { $("button", title:"Save")} //TODO Save Button has no ID
        pcCancelBtn { $("button", title:"Cancel")} //TODO Cancel Button has no ID
        // pcPlanMethod        {} //TODO Plan Methodology label was wrong 'for='. Also value has no reference
    }

    def fillInFields(projInfoMap){
        waitFor(30){pcClientSelector.click()}
        waitFor (1){pcClientItem.click()}
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