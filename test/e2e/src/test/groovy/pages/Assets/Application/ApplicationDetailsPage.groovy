package pages.Assets.Application
import geb.Page

class ApplicationDetailsPage extends Page{

    static at = {
        waitFor {adModalWindow.displayed}
        adModalEditBtn.text().trim() == "Edit"
        adModalDeleteBtn.text().trim() == "Delete"
        adModalCloseBtn
    }

    static content = {
        adModalWindow                   (wait:true) { $("div","aria-describedby":"showEntityView","aria-labelledby":"ui-id-7")}
        adModalTitle                    { adModalWindow.find("span#ui-id-7", class:"ui-dialog-title")}
        adModalPlanningContainer { adModalWindow.find(".dialog .planning-application-table")}
        // TODO Following items fetch by data-content cannot be located as self (Label and Value have the same properties)
        adModalAppName                  { adModalPlanningContainer.find("tr:nth-child(1) td span")[2]}
        adModalDescription              { adModalWindow.find("span","data-content":"Description")}
        adModalSME1                     { adModalWindow.find("span","data-content":"SME1")}
        adModalSME2                     { adModalWindow.find("span","data-content":"SME2")}
        adModalAppOwner                 { adModalWindow.find("span","data-content":"App Owner")}
        adModalBundle                   { adModalWindow.find("span","data-content":"Bundle")}
        adModalPlanStatus               { adModalWindow.find("span","data-content":"Plan Status")}

        adModalSuppColTitles            (required:false) { adModalWindow.find("tr#deps td div",0).find("table thead tr th")}
        adModalSuppList                 (required:false) { adModalWindow.find("tr#deps td div",0).find("table tbody tr")}
        adModalIsDepColTitles           (required:false) { adModalWindow.find("tr#deps td div",1).find("table thead tr th")}
        adModalIsDepList                (required:false) { adModalWindow.find("tr#deps td div",1).find("table tbody tr")}

        //TODO following butttons have no ID to reference them
        adModalEditBtn                  { adModalWindow.find("button", "onclick":contains("EntityCrud.showAssetEditView"))}
        adModalDeleteBtn                { adModalWindow.find("button", name:"_action_Delete")}
        adModalCloneBtn                 { adModalWindow.find("button", name:"_action_clone")}
        adModalAddTaskBtn               { adModalWindow.find("button", "onclick":contains("createIssue('${adModalAppName.text().trim()}',''"))}
        adModalAddCommentBtn            { adModalWindow.find("button", "onclick":contains("createIssue('${adModalAppName.text().trim()}','comment'"))}
        adModalArchGraphBtn             { adModalWindow.find("button", name:"_action_Delete")}
        adModalCloseBtn                 { adModalWindow.find("button", class:"ui-dialog-titlebar-close")}
    }

    def closeDetailsModal(){
        waitFor {adModalCloseBtn.click()}
        waitFor {!adModalWindow.displayed}
    }

    def clickOnCloneButton(){
        waitFor { adModalCloneBtn.click() }
    }

    def getApplicationName(){
        adModalAppName.text().trim()
    }
}
