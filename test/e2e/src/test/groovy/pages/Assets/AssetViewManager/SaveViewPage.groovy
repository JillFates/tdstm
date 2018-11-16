package pages.Assets.AssetViewManager

import geb.Page
import modules.MenuModule
import modules.AssetsModule
import modules.CommonsModule
import modules.CreateViewModule

class SaveViewPage extends Page{

    static at = {
        saveViewModal.displayed
        headerTitle.text() == "Save List View"
    }

    static content = {
        saveViewModal { $(".asset-explorer-view-save-component")}
        headerTitle { saveViewModal.find("h4.modal-title")}
        nameField {saveViewModal.find("input#name")}
        saveBtn {saveViewModal.find("button[type=submit]")}
        menuModule { module MenuModule }
        commonsModule { module CommonsModule }
        createViewModule { module CreateViewModule }
        assetsModule { module AssetsModule }
        sharedView {$("input", name:"shared")}
    }

    def enterName(String value){
        waitFor{nameField.displayed}
        nameField = value
        waitFor{saveBtn.displayed}
    }

    def clickSave(){
        waitFor{saveBtn.click()}
        commonsModule.waitForDialogModalHidden()
        commonsModule.waitForLoader 5 // to save
        commonsModule.waitForLoader 5 // to fill grid content
    }

    def setViewAsShared(){
        if(sharedView.value()!="on"){
            sharedView.click()
        }
        clickSave()
    }

}
