package pages.Planning.Events

/**
 * This class represents the page: <host>.transitionmanager.net/tdstm/moveEvent/list
 * @author: ingrid
 */

import geb.Page
import modules.PlanningMenuModule
import modules.ProjectsMenuModule

class ListEventsPage extends Page {

    static at = {
        listEventsPageTitle.text() == "Event List"
        listEventsPageBreadcrumbs[0].text()   == "Planning"
        listEventsPageBreadcrumbs[1].text()   == "Event List"
    }

    static content = {
        listEventsPageTitle { $("section", class:"content-header").find("h2")}
        planningModule { module PlanningMenuModule}
        listEventsPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        createEventBtn {$("input", value:"Create Event")}
        eventList {$("table#moveEventListIdGrid")}
        rows {eventList.find("tr", role:"row")}
        names {eventList.find("a")}
        nameFilter {$("input#gs_name")}

        projModule {module ProjectsMenuModule}
    }

    def clickCreateEvent(){
        waitFor{createEventBtn.click()}
    }

    def filterEventByName(evtName){
        nameFilter=evtName
    }

    def validateNameIsListed(evtName){
        waitFor{names[0].text()==evtName}
    }

    def validateDescription(desc){
        rows[1].find("td")[2].text()==desc
    }

    def validateStartDate(dt){
        rows[1].find("td")[1].text()==dt
    }

    def validateEventNameAlongProjName(evtNm){
        projModule.projectName.text().contains(evtNm)
    }
}

