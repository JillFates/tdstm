package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class AssetImportETLPage extends Page {

    static at = {
        assetImportPageTitle.text().trim()  == "Import Assets (ETL)"
        assetImportBreadcrumbs[0].text()   == "Assets"

    }

    static content = {
        assetImportPageTitle (wait:true) { $("section", class:"content-header").find("h1")}
        assetImportBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}