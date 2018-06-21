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

    def login() {
        Login log=new Login()
        String credentials=log.readCredentials()
        String usr = credentials.split(",")[0]
        String pass = credentials.split(",")[1]

        /**
         * The following two lines will use the credentials in te testData.txt file unless
         * different credentials are provided when executing.
         */
        username = System.properties['tm.creds.username']?: usr
        password =  System.properties['tm.creds.password'] ?: pass
        submitButton.click()
    }

    def loginSecondaryUser(){
        Login log=new Login()
        String credentials=log.readCredentials()
        String usr = credentials.split(",")[2]
        String pass = credentials.split(",")[3]
        username =  usr
        password =  pass
        submitButton.click()
    }
}
