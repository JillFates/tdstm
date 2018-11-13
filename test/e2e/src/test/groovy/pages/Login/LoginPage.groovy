package pages.Login

import utils.CommonActions
import geb.Page
import utils.Login
import pages.Admin.User.*
import pages.Login.MenuPage

class LoginPage extends Page {
    static url = "/tdstm/auth/login"

    static at = {
        title == "Login"
    }

    static content = {
        username { $("#usernameid") }
        password { $("input", name:"password") }
        submitButton { $("#submitButton") }
        errorMessage { $("#loginForm").find("div", class:"message")}
    }

    def getCredentials(userIndex, passIndex){
        def log=new Login()
        def credentials=log.readCredentials()
        ["user":credentials.split(",")[userIndex], "pass": credentials.split(",")[passIndex]]
    }

    def login(userIndex = 0, passIndex = 1) {
        def userCredentials = getCredentials(userIndex, passIndex)
        /**
         * The following two lines will use the credentials in the testData.txt file unless
         * different credentials are provided when executing.
         */
        username = System.properties['tm.creds.username']?: userCredentials.user
        password =  System.properties['tm.creds.password'] ?: userCredentials.pass
        submitButton.click()
    }

    /*
    Tries to login with a valid username and a wrong password.
    @param: numAttempts  set the number of attempts, after the 3rd one the user gets locked so we use 2 by default
     */
    def loginWrongPass(Integer numAttempts = 2) {
        def log=new Login()
        def credentials=log.readCredentials()

        //On the testDataFile.txt the 6th value is the login_e2e_test_user
        username = credentials.split(",")[6]
        while (numAttempts != 0) {
            password = CommonActions.getRandomString(8)
            submitButton.click()
            verifyWrongPassError()
            numAttempts--
        }
    }

    /*
    Tries to login with an invalid username and a wrong password (which doesn't matter anyway).
    @param: numAttempts  set the number of attempts
     */
    def loginWrongUser(Integer numAttempts = 2) {
        while (numAttempts != 0) {
            username = CommonActions.getRandomString(10)
            password = CommonActions.getRandomString(8)
            submitButton.click()
            verifyWrongUserError()
            numAttempts--
        }
    }

    def verifyWrongPassError(){
        errorMessage.text() == "Username and password are required"
    }

    def verifyWrongUserError(){
        errorMessage.text() == "Invalid username and/or password"
    }

    def verifyLockedUserError(){
        errorMessage.text().contains("Your account is presently locked")
    }

    def lockUsername(userIndex = 6){
        //On the testDataFile.txt we find the username based on the index which is 6 by default (login_e2e_test_user)
        // The loginWrongPass also assumes we're using said user, but I added the parameter to make it extensible
        loginWrongPass(1)

        if(verifyWrongPassError()){ //This means that the username is valid and it is not locked
            while(verifyLockedUserError()!=true)
            {
                loginWrongPass(1)
            }
            true //We break once the user is locked
        } else if(verifyLockedUserError()) //This is the scenario where the username is already locked, so we do nothing
            true
    }
}
