package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class DevicesPage extends Page {

    static at = {
        devicesPageTitle.text().trim()  == "Devices List"
        devicesPageBreadcrumbs[0].text()   == "Assets"
        devicesPageBreadcrumbs[1].text()   == "Devices List"

    }

    static content = {
        devicesPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        devicesPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}