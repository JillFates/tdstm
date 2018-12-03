package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class ManageImportBatchesETLPage extends Page {

    static at = {
        manageImportBatchPageTitle.text().trim()  == "Manage Import Batches (ETL)"
        manageImportBatchBreadcrumbs[0].text()   == "Assets"
        manageImportBatchBreadcrumbs[1].text()   == "Manage Import Batches (ETL)"

    }

    static content = {
        manageImportBatchPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        manageImportBatchBreadcrumbs { $("ol", class:"breadcrumb").find("li")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}