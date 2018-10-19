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
        bundleDetailPageTitle { $("section", class:"content-header").find("h1")}
        updateBtn {$("input.save")}
        cancelBtn {$("input.delete")[0]}

        nameValue {$("input#name")}
        descriptionValue {$("input#description")}
        planningCheck {$("input#useForPlanning")}
        commonsModule {module CommonsModule}
        completionTime {$("#completionTime")}
        startTime {$("#startTime")}
    }

    def editName(text){
        nameValue=nameValue.value()+" "+text
    }

    def editDescription(text){
        descriptionValue=descriptionValue.value()+" "+ text
    }

    def clickCancel(){
        withConfirm(true) {cancelBtn.click() }
    }

    def clickSave(){
        updateBtn.click()
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
}

