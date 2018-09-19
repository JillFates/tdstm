package pages.Assets.AssetViewManager

import geb.Page
import modules.MenuModule
import modules.AssetsModule

class SaveViewPage extends Page{

    static at = {
        saveViewModal.displayed
        headerTitle.text() == "Save List View"
    }

    static content = {
        saveViewModal  (wait:true) { $(".asset-explorer-view-save-component")}
        headerTitle { saveViewModal.find("h4.modal-title")}
        nameField {$("input", id:"name")}
        saveBtn {$("button",text:"Save").not(id:"btnSave")}
        menuModule { module MenuModule }
        assetsModule { module AssetsModule }
        sharedView {$("input", name:"shared")}
    }

    def enterName(String value){
        waitFor{nameField.click()}
        nameField=value
    }
    def clickSave(){
        waitFor{saveBtn.click()}
    }

    def setViewAsShared(){
        if(sharedView.value()!="on"){
            sharedView.click()
        }
        clickSave()
    }

}
