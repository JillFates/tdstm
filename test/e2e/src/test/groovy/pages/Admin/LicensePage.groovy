package pages.Admin

import geb.Page
import modules.AdminModule

class LicensePage extends Page{

    static at = {
        title == "License Admin"
    }

    static content = {
        adminModule { module AdminModule}
    }

}
