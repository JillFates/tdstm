package pages.Planning.Bundle

import geb.Page

/**
 * This class represents the Bundle creation pages, where the form
 * to enter data for new bundle creation is presented.
 * URL:  {host}/tdstm/moveBundle/create
 * @author ingrid
 */
class CreateBundlePage extends Page {

    static at = {
        createBundlesPageTitle.text() == "Create Bundle"
        bundleCreateForm.displayed
    }

    static content = {
        createBundlesPageTitle { $("section", class:"content-header").find("h1")}
        bundleCreateForm {$("[name='bundleCreateForm']")}
        nameField {bundleCreateForm.find("#name")}
        descriptionField {bundleCreateForm.find("#description")}
        fromDropDown {bundleCreateForm.find("#sourceRoomId")}
        toDropDown {bundleCreateForm.find("#targetRoomId")}
        startTimeDatePicker {bundleCreateForm.find("input#startTime")}
        comptimeDatePicker {bundleCreateForm.find("input#completionTime")}
        projectMgrDropDown {bundleCreateForm.find("#projectManagerId")}
        eventMgrDropdown {bundleCreateForm.find("#moveManagerId")}
        orderDropDown {bundleCreateForm.find("#operationalOrder")}
        workFlowDropDown {bundleCreateForm.find("#workflowCode")}
        usePlanningCheck {bundleCreateForm.find("#useForPlanning")}
        saveButton {$("input.save")}
        errorMessages {$("div.errors")}

    }

    def clickSave(){
        saveButton.click()
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
        workFlowDropDown=dataList[2]
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
                       comptimeDatePicker,comptimeDatePicker,projectMgrDropDown,orderDropDown,
                       workFlowDropDown,usePlanningCheck,saveButton]
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

