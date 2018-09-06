package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class StorageLogicalPage extends Page {

    static at = {
        storageLogicalPageTitle.text().trim()  == "Logical Storage List"
        storageLogicalPageBreadcrumbs[0].text()   == "Assets"
        storageLogicalPageBreadcrumbs[1].text()   == "Logical Storage List"

    }

    static content = {
        storageLogicalPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        storageLogicalPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}