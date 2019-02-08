package pages.Planning.Events

import geb.Page
import modules.PlanningMenuModule

class ListEventNewsPage extends Page {

    static at = {
        listEventNewsPageTitle.text() == "Event News"
        listEventNewsPageBreadcrumbs[0].text()   == "Planning"
        listEventNewsPageBreadcrumbs[1].text()   == "News and Issues"
    }

    static content = {
        listEventNewsPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningMenuModule}
        listEventNewsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
    }


}

