package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsMenuModule

class CommentsPage extends Page {

    static at = {
        commentsPageTitle.text().trim()  == "Comments"
        commentsPageBreadcrumbs[0].text()   == "Assets"
        commentsPageBreadcrumbs[1].text()   == "Comments"

    }

    static content = {
        commentsPageTitle (wait:true) { $("section", 	class:"content-header").find("h2")}
        commentsPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        assetsModule { module AssetsMenuModule}
        commonsModule { module CommonsModule }
    }


}