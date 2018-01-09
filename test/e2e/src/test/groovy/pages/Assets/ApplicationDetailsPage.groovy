package pages.Assets
import geb.Page

class ApplicationDetailsPage extends Page{

    static at = {
        adModalTitle.text() == "Application Detail"
        adModalEditBtn.text().trim() == "Edit"
        adModalDeleteBtn.text().trim() == "Delete"
        adModalCloseBtn
        adModalSuppColTitles[0].text().trim() == "Class"
		adModalSuppColTitles[1].text().trim() == "Name"
        adModalSuppColTitles[2].text().trim() == "Bundle"
		adModalSuppColTitles[3].text().trim() == "Type"
        adModalSuppColTitles[4].text().trim() == "Status"
        adModalIsDepColTitles[0].text().trim() == "Class"
        adModalIsDepColTitles[1].text().trim() == "Name"
        adModalIsDepColTitles[2].text().trim() == "Bundle"
        adModalIsDepColTitles[3].text().trim() == "Type"
        adModalIsDepColTitles[4].text().trim() == "Status"
    }

    static content = {

        adModalWindow(wait:true)        { $("div","aria-describedby":"showEntityView")}
        adModalTitle                    { adModalWindow.find("span#ui-id-7", class:"ui-dialog-title") }
// TODO Following items fetch by data-content cannot be located as self (Label and Value have the same properties)
        adModalAppName                  { adModalWindow.find("span","data-content":"Asset Name *")}
        adModalDescription              { adModalWindow.find("span","data-content":"Description")}
        adModalSME1                     { adModalWindow.find("span","data-content":"SME1")}
        adModalSME2                     { adModalWindow.find("span","data-content":"SME2")}
        adModalAppOwner                 { adModalWindow.find("span","data-content":"App Owner")}
        adModalBundle                   { adModalWindow.find("span","data-content":"Bundle")}
        adModalPlanStatus               { adModalWindow.find("span","data-content":"Plan Status")}

        adModalSuppColTitles            { adModalWindow.find("tr#deps td div",0).find("table thead tr th")}
        adModalSuppList                 { adModalWindow.find("tr#deps td div",0).find("table tbody tr")}
        adModalIsDepColTitles           { adModalWindow.find("tr#deps td div",1).find("table thead tr th")}
        adModalIsDepList                { adModalWindow.find("tr#deps td div",1).find("table tbody tr")}

//TODO following butttons have no ID to reference them
        adModalEditBtn                  { adModalWindow.find("button", "onclick":contains("EntityCrud.showAssetEditView"))}
        adModalDeleteBtn                { adModalWindow.find("button", name:"_action_Delete")}
        adModalCloneBtn                 { adModalWindow.find("button", name:"_action_clone")}
        adModalAddTaskBtn               { adModalWindow.find("button", "onclick":contains("createIssue('${adModalAppName.text().trim()}',''"))}
        adModalAddCommentBtn            { adModalWindow.find("button", "onclick":contains("createIssue('${adModalAppName.text().trim()}','comment'"))}
        adModalArchGraphBtn             { adModalWindow.find("button", name:"_action_Delete")}
        adModalCloseBtn                 { adModalWindow.find("button", class:"ui-dialog-titlebar-close")}

    }



}
