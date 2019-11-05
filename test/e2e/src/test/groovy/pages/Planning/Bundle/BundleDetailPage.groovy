package pages.Planning.Bundle

import geb.Page
import modules.PlanningMenuModule
import modules.MenuModule

/**
 * This class represents the bundle teatils page, where the details of a bundle are displayed.
 * URL:  {host}/tdstm/moveBundle/show/<bundleID>
 */

class BundleDetailPage extends Page {

    static at = {
        bundleDetailPageTitle.text() == "Bundle Detail"
    }

    static content = {
        bundleDetailPageTitle { $("bundle-view-edit-component").find("h4")}
        details {$("div.steps_table")[0]}

        creationMessage {details.find("div.message")}
        nameValue {details.find(".name", text:"Name:").next()}

        descriptionValue {details.find(".name", text:"Description:").next()}
        planningModule {module PlanningMenuModule }
        menuModule {module MenuModule }
        planningCheck {$("input#useForPlanning")[0]}
        listbundles {$("a.list")}
        message {$("div.message")}
        startTime {$(".name", text:"Start Time:")next()}
        completionTime {$(".name", text: "Completion Time:").next()}
        //buttons section
        btnsContainer {$("form")}
        editBtn {$('tds-button-edit')}
        deleteBtn {$('tds-button-delete')}
        deleteBundleAndAssets {$("button.btn-danger")}
        //Confirnation popup
        confirmBtn (required:false){$('button.btn.btn-primary.pull-left')[2]}
        cancelBtn  (required:false) { $('button.btn.btn-default.pull-right')}
        closeButton {$('tds-button-close')}
    }

    def closeModal(){
        waitFor{closeButton.click()}
        sleep(2000)
    }

    def cancelDeletion(){
        waitFor{deleteBtn.click()}
        waitFor{ cancelBtn.click()}
    }

    def confirmDeletion(){
        waitFor{deleteBtn.click()}
        sleep(1000)
        waitFor{confirmBtn.click()}
    }

    def clickDelete(){
        delete.click()
    }

    def clickEdit(){
        editBtn.click()
    }

    def isPlanning(){
        planningCheck.value()
    }

    def validateNameDescription(data){
        def allFieldsAsExpected=true
        def dispData=[nameValue.text(),descriptionValue.text()]
        for (it in dispData) {
            if (!data.contains(it)) {
                allFieldsAsExpected=false
                break
            }
        }
        allFieldsAsExpected
    }

    def validateDataDisplayed(data){
        def allFieldsAsExpected=true
        def dispData=[nameValue.text(),descriptionValue.text()]
        if(isPlanning()!=data[3]){
            allFieldsAsExpected=false
        }else{
            for (it in dispData) {
                if (!data.contains(it)) {
                    allFieldsAsExpected=false
                    break
                }
            }
        }
        allFieldsAsExpected
    }


    def bundleCreatedMsgIsDisplayed(data){
        creationMessage.text()== "MoveBundle "+data[0]+" created"
    }
    /**
     * Returns bundle data displayed
     */
    def getDataDisplayed(){
        def dispData=[nameValue.text(),descriptionValue.text(),isPlanning()]
        dispData
    }
    /**
     * Validates that the original bundle data has been edited and the changes were saved
     * @param origData     *
     */
    def dataIsEdited(origData){
        def dispData=[nameValue.text(),descriptionValue.text()]
        //assert(isPlanning()!=origData[3])
        assert(origData[0]+" Edited"== dispData[0])
        assert(origData[1]+" Edited"== dispData[1])
        assert(origData[2]== dispData[2])
        assert (startTime.text().compareTo(origData[4])== 0 )
        assert(completionTime.text().compareTo(origData[5])==0)
        return true
    }

    def validateUpdateMesage(name){
       message.text()=="MoveBundle "+name+" updated"
    }

    def nameDescriptionEdited(origData){
        def dispData=[nameValue.text(),descriptionValue.text()]
        (origData[0]+" Edited"== dispData[0]) && (origData[1]+" Edited"== dispData[1])
    }
}

