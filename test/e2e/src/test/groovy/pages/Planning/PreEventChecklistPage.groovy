package pages.Planning

import geb.Page
import modules.PlanningModule

class PreEventChecklistPage extends Page {

    static at = {
        modaltitle.text() == "Pre-Event Checklist"
        preEventChecklistPageBreadcrumbs[0].text()   == "Reports"
        preEventChecklistPageBreadcrumbs[1].text()   == "Pre-Event Checklist"
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningModule}
        preEventChecklistPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
    }


}

