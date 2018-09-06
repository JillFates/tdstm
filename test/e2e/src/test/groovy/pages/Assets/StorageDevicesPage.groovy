package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class StorageDevicesPage extends Page {

    static at = {
        storageDevicePageTitle.text().trim()  == "Storage Device List"
        storageDevicePageBreadcrumbs[0].text()   == "Assets"
        storageDevicePageBreadcrumbs[1].text()   == "Storage Device List"

    }

    static content = {
        storageDevicePageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        storageDevicePageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}