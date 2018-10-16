package pages.Planning

import geb.Page
import modules.PlanningModule

class EventDetailsPage extends Page {

    static at = {
        eventDetailsPageTitle.text() == "Event Details"
        eventDetailsPageBreadcrumbs[0].text()   == "Planning"
        eventDetailsPageBreadcrumbs[1].text()   == "Event"
        eventDetailsPageBreadcrumbs[2].text()   == "Details"
    }

    static content = {
        eventDetailsPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningModule}
        eventDetailsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
    }


}
