package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class ArchitectureGraphPage extends Page {

    static at = {
        architecturePageTitle.text().trim()  == "Architecture Graph"
        architecturePageBreadcrumbs[0].text()   == "Assets"
        architecturePageBreadcrumbs[1].text()   == "Architecture"

    }

    static content = {
        architecturePageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        architecturePageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}