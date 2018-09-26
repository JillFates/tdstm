package pages.Planning

import geb.Page
import modules.PlanningModule

class ListEventNewsPage extends Page {

    static at = {
        modaltitle.text() == "Event News"
        listEventNewsPageBreadcrumbs[0].text()   == "Planning"
        listEventNewsPageBreadcrumbs[1].text()   == "News and Issues"
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningModule}
        listEventNewsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
    }


}

