package pages.AssetViewManager
import geb.Page
import modules.MenuModule
import modules.AssetsModule

class SaveViewPage extends Page{

    static at = {
        waitFor {saveViewModal.displayed}
    }

    static content = {
        saveViewModal  (wait:true) { $("section","class":"content-header")}
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
