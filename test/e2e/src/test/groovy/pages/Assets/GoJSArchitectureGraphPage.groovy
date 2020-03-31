package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class GoJSArchitectureGraphPage extends Page {

    static at = {
        architecturePageTitle.text().trim()  == "Architecture Graph"
        architecturePageBreadcrumbs[0].text()   == "Assets"
        architecturePageBreadcrumbs[1].text()   == "Go JS Architecture Graph"

    }

    static content = {
        architecturePageTitle (wait:true) { $("section", 	class:"content-header").find("h2")}
        architecturePageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}
