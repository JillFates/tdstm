package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class ServersPage extends Page {

    static at = {
        serversPageTitle.text().trim()  == "Server List"
        serversPageBreadcrumbs[0].text()   == "Assets"
        serversPageBreadcrumbs[1].text()   == "Server List"

    }

    static content = {
        serversPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        serversPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}