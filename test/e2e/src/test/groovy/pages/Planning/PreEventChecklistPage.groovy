package pages.Planning

import geb.Page
import modules.PlanningMenuModule

class PreEventChecklistPage extends Page {

    static at = {
        preEventPageTitle.text() == "Pre-Event Checklist"
        preEventChecklistPageBreadcrumbs[0].text()   == "Planning"
        preEventChecklistPageBreadcrumbs[1].text()   == "Pre-Event Checklist"
    }

    static content = {
        preEventPageTitle { $("section", class:"content-header").find("h2")}
        planningModule { module PlanningMenuModule}
        preEventChecklistPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
    }


}

