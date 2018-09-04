package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class ManageImportBatchesExcelPage extends Page {

    static at = {
        manageImportBatchPageTitle.text().trim()  == "Manage Import Batches (Excel)"
        manageImportBatchBreadcrumbs[0].text()   == "Assets"

    }

    static content = {
        manageImportBatchPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        manageImportBatchBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}