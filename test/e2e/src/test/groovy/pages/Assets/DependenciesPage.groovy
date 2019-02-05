package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class DependenciesPage extends Page {

    static at = {
        dependenciesPageTitle.text().trim()  == "Dependencies List"
        dependenciesPageBreadcrumbs[0].text()   == "Assets"
        dependenciesPageBreadcrumbs[1].text()   == "Dependencies"

    }

    static content = {
        dependenciesPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        dependenciesPageBreadcrumbs { $("ol", class:"breadcrumb").find("li")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}