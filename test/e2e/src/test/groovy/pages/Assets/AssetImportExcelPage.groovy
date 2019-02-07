package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class AssetImportExcelPage extends Page {

    static at = {
        assetImportPageTitle.text().trim()  == "Import Assets (TM Excel)"
        assetImportBreadcrumbs[0].text()   == "Assets"

    }

    static content = {
        assetImportPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        assetImportBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}