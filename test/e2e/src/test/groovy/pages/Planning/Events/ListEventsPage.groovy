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

    /*
        The estimated start date works on a 12-hour format. This means that you can create an event with the following date:
        11/11/2019 09:18 AM - Which is a problem because the Event List page displays times without the first 0, on the Event
        List page that date would be displayed as  "11/11/2019 9:18 AM". This means that if we compare the date we used to create
        it and the date that is displayed, the string is NOT the same.
        For this reason we have to account for this and check the value displayed, the parameter that was sent and transform
        it if necessary.
        @author: Alvaro Navarro
     */
    def validateStartDate(dt){

        def rowDateValue
        rowDateValue = rows[1].find("td", "aria-describedby":"moveEventListIdGrid_estStartTime").text()

        def onlyDateValue
        def onlyTimeValue
        onlyDateValue = dt.substring(0, 11)
        onlyTimeValue = dt.substring(dt.length()-8)

        if(onlyTimeValue[0].equals("0"))
        {
            dt = onlyDateValue + onlyTimeValue.substring(1 , onlyTimeValue.length())
        }

        rowDateValue.equals(dt)
    }

    def validateEventNameAlongProjName(evtNm){
        projModule.projectName.text().contains(evtNm)
    }
}

