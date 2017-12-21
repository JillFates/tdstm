package pages.common

import geb.Page
import modules.MenuModule

class MenuPage extends Page {

    static at = {
        title.contains("User Dashboard For")
    }

    static content = {
        menuModule { module MenuModule }
    }
}
