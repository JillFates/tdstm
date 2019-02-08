package pages.Planning

import geb.Page
import modules.PlanningMenuModule

class ExportRunbookPage extends Page {

    static at = {
        exportRunbookPageTitle.text() == "Export Runbook"
        exportRunbookPageBreadcrumbs[0].text()   == "Planning"
        exportRunbookPageBreadcrumbs[1].text()   == "Export Runbook"
    }

    static content = {
        exportRunbookPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningMenuModule}
        exportRunbookPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
    }


}

