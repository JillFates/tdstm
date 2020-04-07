package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class ManageImportBatchesExcelPage extends Page {

    static at = {
        manageImportBatchPageTitle.text().trim()  == "Manage Import Batches (TM Excel)"
        manageImportBatchBreadcrumbs[0].text()   == "Assets"

    }

    static content = {
        manageImportBatchPageTitle (wait:true) { $("section", 	class:"content-header").find("h2")}
        manageImportBatchBreadcrumbs { $("ol", class:"legacy-breadcrumb").find("li a")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}