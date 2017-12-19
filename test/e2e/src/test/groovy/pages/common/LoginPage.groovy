package pages.common

import geb.Page
import modules.LoginModule
import pages.Dashboards.UserDashboardPage

class LoginPage extends Page {
    static url = "/tdstm/auth/login"

    static at = {
        title == "Login"
    }

    static content = {
        loginModule { module LoginModule }
    }
}
