package pages.Planning.Bundle

import geb.Page
import modules.CommonsModule
import java.text.SimpleDateFormat

/**
 * This class represents the bundle edit page, where the details of a bundle are displayed.
 * URL:  {host}/tdstm/moveBundle/list/
 */

class EditBundlePage extends Page {

    static at = {
        bundleDetailPageTitle.text() == "Bundle Edit"
    }

    static content = {
        bundleDetailPageTitle {$('bundle-view-edit-component').find("h4.modal-title")}
        saveBtn {$("button.tds-button-save")}
        cancelBtn {$('button.tds-button-cancel')}

        nameValue {$("input#name")}
        descriptionValue {$("input#description")}
        planningCheck {$("input#useForPlanning")}
        commonsModule {module CommonsModule}
        completionTime {$("#completionTime")}
        startTime {$("#startTime")}
        confirmBtn (required:false){$('button.btn.btn-primary.pull-left')[1]}
        closeButton {$('tds-button-close')}
    }

    def editName(text){
        nameValue=nameValue.value()+" "+text
    }

    def editDescription(text){
        descriptionValue=descriptionValue.value()+" "+ text
    }

    def clickCancel(){
        waitFor{ cancelBtn.click()}
        sleep(1000)
    }

    def cancelEdition(){
        waitFor{ cancelBtn.click()}
        sleep(1000)
        waitFor{confirmBtn.click()}
        sleep(1000)
    }

    def clickSave(){
        sleep(1000)
        saveBtn.click()
        sleep(1000)
    }

    def changeIsPlanningValue(){
        planningCheck.click()
    }
    /**
     * Adds today's date as start date and tomorrow's date as completion Date
     */
    def editDates(){
        def today = new Date()
        def tomorrow = today + 1
        startTime = today.format("MM/dd/YYYY hh:mm aaa")
        completionTime = tomorrow.format("MM/dd/YYYY hh:mm aaa")
        def dateSet= [today.format("MM/dd/YYYY hh:mm aaa"),tomorrow.format("MM/dd/YYYY hh:mm aaa")]
        dateSet
    }

    def clickClose(){
        closeButton.click()
        sleep(1000)
    }
}

