package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class DependencyAnalyzerPage extends Page {

    static at = {
        depenAnalyzerPageTitle.text().trim()  == "Dependency Analyzer"
        depenAnalyzerPageBreadcrumbs[0].text()   == "Assets"
        depenAnalyzerPageBreadcrumbs[1].text()   == "Dependency Analyzer"

    }

    static content = {
        depenAnalyzerPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        depenAnalyzerPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}