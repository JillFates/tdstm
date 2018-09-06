package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class DatabasesPage extends Page {

    static at = {
        databasesPageTitle.text().trim()  == "Database List"
        databasesPageBreadcrumbs[0].text()   == "Assets"
        databasesPageBreadcrumbs[1].text()   == "Database List"

    }

    static content = {
        databasesPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        databasesPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}