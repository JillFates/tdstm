package pages.Login

import utils.CommonActions
import geb.Page
import utils.Login
import pages.Admin.User.*
import pages.Login.MenuPage
import modules.CommonsModule

class LoginPage extends Page {
    static url = "/tdstm/module/auth/login"

    static at = {
        title == "Login"
        waitFor(8){username.displayed}
        waitFor(8){password.displayed}
        //waitFor(8){submitButton.displayed}
    }

    static content = {
        domain {$("select", name:"authority")}
        domainSelectorValues(wait:true, required:false) { $("option")}
        username { $("#usernameid") }
        password { $("input", name:"password") }
        submitButton { $("tds-button#loginBtn") }
        errorMessage { $("span", class:"alert-text")}
        commonsModule { module CommonsModule }
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
        selectDomain()
        username = System.properties['tm.creds.username']?: userCredentials.user
        password =  System.properties['tm.creds.password'] ?: userCredentials.pass
        submitButton.click()
    }

    /*
    Tries to login with a valid username and a wrong password.
    @param: numAttempts  set the number of attempts, after the 3rd one the user gets locked so we use 2 by default
     */
    def loginWrongPass(Integer numAttempts = 2) {
        selectDomain()
        def log=new Login()
        def credentials=log.readCredentials()

        //On the testDataFile.txt the 6th value is the login_e2e_test_user
        username = credentials.split(",")[6]
        while (numAttempts != 0) {
            password = CommonActions.getRandomString(8)
            submitButton.click()
            verifyWrongPassError()
            numAttempts--
            commonsModule.waitForLoader(5)
        }
    }

    /*
    Tries to login with an invalid username and a wrong password (which doesn't matter anyway).
    @param: numAttempts  set the number of attempts
     */
    def loginWrongUser(Integer numAttempts = 2) {
        selectDomain()
        while (numAttempts != 0) {
            username = CommonActions.getRandomString(10)
            password = CommonActions.getRandomString(8)
            submitButton.click()
            verifyWrongUserError()
            numAttempts--
            commonsModule.waitForLoader(10)
        }
    }

    def verifyWrongPassError(){
        waitFor(2){errorMessage.displayed}
        errorMessage.text() == "Username and password are invalid"
    }

    def verifyWrongUserError(){
        waitFor(2){errorMessage.displayed}
        errorMessage.text() == "Invalid username and/or password"
    }

    def verifyLockedUserError(){
        waitFor(2){errorMessage.displayed}
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

    def selectDomain(){
        domain.click()
        waitFor { domainSelectorValues.size() > 0 }
        domainSelectorValues[1].click()
    }
}
