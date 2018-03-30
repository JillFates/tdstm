package pages.Login

import geb.Page
import modules.AdminModule
import modules.MenuModule

class MenuPage extends Page {

    static at = {
        contextPath.value() == "/tdstm"
    }

    static content = {
        contextPath { $("input",id:"contextPath")}
        menuModule { module MenuModule}
        adminModule { module AdminModule}
    }
}
