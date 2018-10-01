package pages.Planning

import geb.Page
import modules.PlanningModule

class ExportRunbookPage extends Page {

    static at = {
        exportRunbookPageTitle.text() == "Export Runbook"
        exportRunbookPageBreadcrumbs[0].text()   == "Planning"
        exportRunbookPageBreadcrumbs[1].text()   == "Export Runbook"
    }

    static content = {
        exportRunbookPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningModule}
        exportRunbookPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
    }


}

