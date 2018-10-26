package specs.Login

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions
import pages.Admin.User.*


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
        and: "Tries to Login with an invalid username a random number of times"
            loginWrongUser(randomAttemptNumber)
        when: "The user tries to login with the valid username from above and the correct password"
            //On the testDataFile.txt the 6th and 7th values are the login_e2e_test_user and its correct password
            login(6,7)
        then: "The user is successfully logged in"
            at MenuPage
    }

    def "4. Lock the login_e2e_test_user@dev.com user"(){
        given: "The User goes to Login page"
            to LoginPage
        when: "The login_e2e_test_user@dev.com username is locked"
            /*
                This means that we will use that username and try to login as many times as necessary until we lock it.
                We're sending a 6 because on the testDataFile.txt the 6th position is the login_e2e_test_user.
             */
            lockUsername()
        then: "We remain on the Login Page until another admin user unlocks the username"
            at LoginPage
    }

    def "5. Verify with an admin that the login_e2e_test_user@dev.com user is locked"(){
        given: "The User goes to Login page"
            to LoginPage
        and: "An admin logs in (we assume the e2e_test_user is being used)"
            login()
        and: "The Admin user goes to the List Users page (e2e_test_user by default)"
            at MenuPage
            adminModule.goToListUsers()
            at UserListPage
        and: "The username is filtered"
            filterByUsername("login_e2e_test_user@dev.com")
        and: "The locked icon is displayed"
            lockedIconDisplayed()
        and: "We open the locked user by clicking on its username"
            clickOnFirstUserName()
        when: "The User Details Page is displayed"
            at UserDetailsPage
        then: "We verify that the Unlock icon/legend is displayed"
            verifyUnlockButtonDisplayed()
    }
}


