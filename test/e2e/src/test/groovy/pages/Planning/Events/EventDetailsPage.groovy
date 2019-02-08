package pages.Planning.Events

/**
 * This class represents the page: <host>.transitionmanager.net/tdstm/moveEvent/show/<eventID>
 * @author: ingrid
 */

import geb.Page
import modules.PlanningMenuModule

class EventDetailsPage extends Page {

    static at = {
        eventDetailsPageTitle.text() == "Event Details"
        eventDetailsPageBreadcrumbs[0].text()   == "Planning"
        eventDetailsPageBreadcrumbs[1].text()   == "Event"
        eventDetailsPageBreadcrumbs[2].text()   == "Details"
    }

    static content = {
        eventDetailsPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningMenuModule}
        eventDetailsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        eventCreatedMessage (required:false) {$("div.message")[0]}
        eventListBtn {$("a.list")}

        dialog {$("div.dialog")}
        nameValue {dialog.find(".valueNW")[0]}
        descriptionValue {dialog.find(".valueNW")[1]}
        startDateValue {dialog.find(".name", text:"Estimated Start:").next()}

    }

    def validateEventName(name){
        nameValue.text()==name
    }

    def validateEventDescription(desc){
        descriptionValue.text()==desc
    }
    def validateEventStartDate(dte){
        startDateValue.text()==dte
    }

    def validateEventCreationMessage(evntName){
        eventCreatedMessage.text()=="MoveEvent "+evntName+" created"
    }

    def goToEventList(){
        eventListBtn.click()
    }



}

