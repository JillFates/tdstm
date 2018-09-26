package pages.Planning

import geb.Page
import modules.PlanningModule

class ListBundlesPage extends Page {

    static at = {
        modaltitle.text() == "Bundle List"
        listBundlesPageBreadcrumbs[0].text()   == "Planning"
        listBundlesPageBreadcrumbs[1].text()   == "Bundles"
        listBundlesPageBreadcrumbs[2].text()   == "List"
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningModule}
        listBundlesPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
    }


}

