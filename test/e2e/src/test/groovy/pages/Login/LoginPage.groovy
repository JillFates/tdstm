package pages.Login

import geb.Page
import utils.Login

class LoginPage extends Page {
    static url = "/tdstm/auth/login"

    static at = {
        title == "Login"
    }

    static content = {
        username        { $("#usernameid") }
        password        { $("input", name:"password") }
        submitButton    { $("#submitButton") }
    }

    def getCredentials(userIndex, passIndex){
        def log=new Login()
        def credentials=log.readCredentials()
        ["user":credentials.split(",")[userIndex], "pass": credentials.split(",")[passIndex]]
    }

    def login(userIndex = 0, passIndex = 1) {
        def userCredencials = getCredentials(userIndex, passIndex)
        /**
         * The following two lines will use the credentials in te testData.txt file unless
         * different credentials are provided when executing.
         */
        username = System.properties['tm.creds.username']?: userCredencials.user
        password =  System.properties['tm.creds.password'] ?: userCredencials.pass
        submitButton.click()
    }
}
