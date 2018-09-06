package pages.Assets

import geb.Page
import modules.CommonsModule
import modules.AssetsModule

class CommentsPage extends Page {

    static at = {
        commentsPageTitle.text().trim()  == "Asset Comment"
        commentsPageBreadcrumbs[0].text()   == "Assets"
        commentsPageBreadcrumbs[1].text()   == "Comments"

    }

    static content = {
        commentsPageTitle (wait:true) { $("section", 	class:"content-header").find("h1")}
        commentsPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
    }


}