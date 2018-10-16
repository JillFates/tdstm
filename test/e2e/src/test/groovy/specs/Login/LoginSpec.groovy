package specs.Login

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class LoginSpec extends GebReportingSpec {

    static randomAttemptNumber = CommonActions.getRandomNumber(15)

    def "1. Login to Transition Manager - Happy Path"(){
        given: "The User goes to Login page"
            to LoginPage
        when: "The user tries to login with his credentials"
            login() //credentials are taken from config file inside method
        then: "The user should be logged in and menu should be displayed"
            at MenuPage
    }

    def "2. Login to Transition Manager with a wrong password twice - Negative Scenario"(){
        given: "The User goes to Login page"
            to LoginPage
        and: "Tries to Login twice with a valid username and a wrong random password"
            loginWrongPass(2)
        when: "The user tries to login with the valid username from above and the correct password"
        //On the testDataFile.txt the 6th and 7th values are the login_e2e_test_user and its correct password
            login(6,7)
        then: "The user is successfully logged in"
            at MenuPage
    }

    def "3. Login to Transition Manager with an invalid username a random number of times - Negative Scenario"(){
        given: "The User goes to Login page"
            to LoginPage
        and: "Tries to Login twice with an invalid username a random number of times"
            loginWrongUser(randomAttemptNumber)
        when: "The user tries to login with the valid username from above and the correct password"
            //On the testDataFile.txt the 6th and 7th values are the login_e2e_test_user and its correct password
            login(6,7)
        then: "The user is successfully logged in"
            at MenuPage
    }
}


