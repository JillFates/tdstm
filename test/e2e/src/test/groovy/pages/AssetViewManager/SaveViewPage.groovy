package pages.AssetViewManager
import geb.Page
import modules.MenuModule

class SaveViewPage extends Page{

    static at = {
        waitFor {saveViewModal.displayed}
    }

    static content = {
        saveViewModal                (wait:true) { $("section","class":"content-header")}
        nameField                    {$("input", id:"name")}
        saveBtn                      {$("button",text:"Save").not(id:"btnSave")}
        menuModule                   { module MenuModule }
    }

    def enterName(String value){
        nameField.click()
        nameField=value
    }
    def clickSave(){
        saveBtn.click()
    }

}
