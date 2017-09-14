package pages.common

import geb.Page
import pages.Cookbook.UserDashboardPage

//import modules.ManualsMenuModule

class LoginPage extends Page {
    static url = "/tdstm/auth/login"

    static at = { title == "Login" }

    static content = {
        username { $("#usernameid") }
        password { $("input", name:"password") }
        submitButton(to: UserDashboardPage) { $("#submitButton") }
        //manualsMenu { module(ManualsMenuModule) }
    }
}
