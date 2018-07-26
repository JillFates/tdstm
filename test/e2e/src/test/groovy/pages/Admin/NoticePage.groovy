package pages.Admin

import geb.Page
import modules.AdminModule

class NoticePage extends Page {


    static at = {
        title == "Notice Administration"
        pageHeaderName.text() == "Notice Administration"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header ng-scope").find("h1")}
    }

}
