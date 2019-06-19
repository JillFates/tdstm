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
        listEventsPageBreadcrumbs[1].text()   == "Event"
        listEventsPageBreadcrumbs[2].text()   == "List"
    }

    static content = {
        listEventsPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningMenuModule}
        listEventsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
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
        waitFor{rows[1].find("td")[3].text()==desc}
    }

    def validateStartDate(dt){
        rows[1].find("td", "aria-describedby":"moveEventListIdGrid_estStartTime").text()
        def rowDateValue
        rowDateValue = rows[1].find("td", "aria-describedby":"moveEventListIdGrid_estStartTime").text()
        println "rodw date value is: " + rowDateValue
        println "parameter dt is: " + dt
        rowDateValue.equals(dt)
    }

    def validateEventNameAlongProjName(evtNm){
        projModule.projectName.text().contains(evtNm)
    }
}

