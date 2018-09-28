package pages.Planning

import geb.Page
import modules.PlanningModule

class ListEventsPage extends Page {

    static at = {
        listEventsPageTitle.text() == "Event List"
        listEventsPageBreadcrumbs[0].text()   == "Planning"
        listEventsPageBreadcrumbs[1].text()   == "Event"
        listEventsPageBreadcrumbs[2].text()   == "List"
    }

    static content = {
        listEventsPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningModule}
        listEventsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
    }


}

