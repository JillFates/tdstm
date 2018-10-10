package pages.Planning.Bundle

import geb.Page
import modules.PlanningModule
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
        bundleDetailPageTitle { $("section", class:"content-header").find("h1")}
        details {$("div.steps_table")[0]}

        creationMessage {details.find("div.message")}
        nameValue {details.find(".name", text:"Name:").next()}
        workFlowCodeValue {details.find(".name", text:"WorkFlow Code").next()}
        descriptionValue {details.find(".name", text:"Description:").next()}
        planningModule {module PlanningModule }
        menuModule {module MenuModule }
        planningCheck {$("input#useForPlanning")}
    }

    def isPlanning(){
        planningCheck.value()
    }

    def validateDataDisplayed(data){
        def allFieldsAsExpected=true
        def dispData=[nameValue.text(),descriptionValue.text(),workFlowCodeValue.text()]
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

}

