package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class DependenciesPage extends Page {

    static at = {
        dependenciesPageTitle.text().trim()  == "Dependencies"
        dependenciesPageBreadcrumbs[0].text()   == "Assets"
        dependenciesPageBreadcrumbs[1].text()   == "Dependencies"

    }

    static content = {
        dependenciesPageTitle (wait:true) { $("section", 	class:"content-header").find("h2")}
        dependenciesPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}