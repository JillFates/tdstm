package pages.Planning.Events

import geb.Page
import modules.PlanningMenuModule
import modules.CommonsModule

/**
 * This class represents the create event page: Planing>List Events> Create Event
 * URL: {host}/tdstm/moveEvent/create
 * @author ingrid
 */

class CreateEventPage extends Page {

    static at = {
        eventCreationPageTitle.text() == "Create Event"
        eventDetailsPageBreadcrumbs[0].text()   == "Planning"
        eventDetailsPageBreadcrumbs[1].text()   == "Event"
        eventDetailsPageBreadcrumbs[2].text()   == "Create"
    }

    static content = {
        eventCreationPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningMenuModule}
        eventDetailsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        bundleList {$("#moveBundleList")}
        nameField {$("#name")}
        descriptionField {$("#description")}
        tagSelector {$("div.k-multiselect-wrap")}
        saveBtn {$("input", class:"save")}
        cancelBtn {$("input", class:"cancel")}
        runbookBdg1 {$("#runbookBridge1")}
        runbookBdg2 {$("#runbookBridge2")}
        runBookStatus {$("#runbookStatus")}
        startTime {$("#kendoEstStartTime")}

        commons {module CommonsModule}

    }

    def cancelEventCreation(){
        cancelBtn.click()
    }

    def saveChanges(){
        saveBtn.click()
    }

    def completeForm(data){
        enterName(data[0])
        enterDescription(data[1])
        enterStartDate()
    }

    def saveEvent(){
        saveBtn.click()
    }

    def createEvent(){
        completeForm()
        saveEvent()
    }

    def  enterName(nm){
        nameField=nm
    }

    def enterDescription(desc){
        descriptionField=desc
    }

    def enterRunBook1(txt){
        runbookBdg1=txt
    }

    def enterRunBook2(txt){
        runbookBdg2=txt
    }

    def selectRunBookStatus(){
        runBookStatus.selectKendoDropdownOptionByText("Draft")
    }

    def enterStartDate(){
        def today = new Date()
        startTime = today.format("MM/dd/YYYY hh:mm aaa")
    }

}

