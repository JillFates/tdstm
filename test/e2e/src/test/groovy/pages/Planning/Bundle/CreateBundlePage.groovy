package pages.Planning.Bundle

import geb.Page
import geb.*

/**
 * This class represents the Bundle creation pages, where the form
 * to enter data for new bundle creation is presented.
 * URL:  {host}/tdstm/moveBundle/create
 * @author ingrid
 */
class CreateBundlePage extends Page {

    static at = {
        createBundlesPageTitle.text() == "Bundle Create"
        bundleCreateForm.displayed
    }

    static content = {
        createBundlesPageTitle { bundleCreateForm.find("h4")}
        bundleCreateForm {$("#bundle-create-component")}
        nameField {$("#name")}
        descriptionField {$("#description")}
        fromDropDown {$("kendo-dropdownlist#sourceRoomId")}
        toDropDown {$("#targetRoomId")}
        startTimeDatePicker {$("[name='startTime']")}
        comptimeDatePicker {$("[name='completionTime']")}

        orderDropDown {$("#operationalOrderId")}
        usePlanningCheck {$("#useForPlanning")}
        saveButton {$("button.tds-button-save")}
        errorMessages {$("div.errors")}

    }

    def clickSave(){
        saveButton.click()
    }

    def validateSaveIsDisabled(){
        $('tds-button-save.btn-primary.pull-left.tds-generic-button.tds-action-button--disabled').displayed
    }

    def enterName(text){
        nameField=text
    }
    def clearName(){
        nameField=""
    }

    def enterBundleData(dataList){
        nameField=dataList[0]
        descriptionField=dataList[1]
    }

    def clickPlanning(){
        usePlanningCheck.click()
    }

    /**
     * For each of the fields in the list validates it is displayed.
     * If any of the fields is not displayed then the method wil return FALSE
     */
    def validatePresentFields(){
        def allFieldsPresent=true
        def fieldList=[nameField,descriptionField,fromDropDown,toDropDown,startTimeDatePicker,
                       comptimeDatePicker,comptimeDatePicker,orderDropDown,
                       usePlanningCheck,saveButton]
        for (it in fieldList) {
            if (!it.displayed) {
                allFieldsPresent=false
                break
            }
        }
        allFieldsPresent
    }

    def validateNameMessage(){
        errorMessages.find("li", text:"Name cannot be blank").displayed
    }

    def validateWorkFlowMessage(){
        errorMessages.find("li", text:"Please select WorkflowCode").displayed
    }

    def isPlanning(){
        usePlanningCheck.value()
    }
}

