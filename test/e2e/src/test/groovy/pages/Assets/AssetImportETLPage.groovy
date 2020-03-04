package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class AssetImportETLPage extends Page {

    static at = {
        assetImportPageTitle.text()  == "Import Assets (ETL)"
        assetImportBreadcrumbs[0].text()   == "Assets"

    }

    static content = {
        assetImportPageTitle (wait:true){$("section.content-header h2")}
        assetImportBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}