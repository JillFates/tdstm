package pages.Assets
import geb.Page

class ApplicationDetailsPage extends Page{

    static at = {
        adModalTitle.text() == "Application Detail"
        adModalEditBtn.text().trim() == "Edit"
        adModalDeleteBtn.text().trim() == "Delete"
        adModalCloseBtn
    }

    static content = {

        adModalWindow(wait:true)        { $("div","aria-describedby":"showEntityView")}
        adModalTitle                    { adModalWindow.find("span#ui-id-7", class:"ui-dialog-title") }
// TODO Following items selected by data-content cannot be located as self (Label and Value have the same properties)
        adModalAppName                  { adModalWindow.find("span","data-content":"Asset Name *")}
        adModalDescription              { adModalWindow.find("span","data-content":"Description")}
        adModalSME1                     { adModalWindow.find("span","data-content":"SME1")}
        adModalSME2                     { adModalWindow.find("span","data-content":"SME2")}
        adModalAppOwner                 { adModalWindow.find("span","data-content":"App Owner")}
        adModalBundle                   { adModalWindow.find("span","data-content":"Bundle")}
        adModalPlanStatus               { adModalWindow.find("span","data-content":"Plan Status")}
//TODO following butttons have no ID to reference them
        adModalEditBtn                  { adModalWindow.find("button", "onclick":contains("EntityCrud.showAssetEditView"))}
        adModalDeleteBtn                { adModalWindow.find("button", name:"_action_Delete")}
        adModalCloseBtn                 { adModalWindow.find("button", class:"ui-dialog-titlebar-close")}

    }



}
