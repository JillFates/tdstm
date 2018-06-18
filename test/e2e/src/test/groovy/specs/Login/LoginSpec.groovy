package specs.Login

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class LoginSpec extends GebReportingSpec {

    def "1. Login to Transition Manager"(){
        given: "The User goes to Login page"
            to LoginPage
        when: "The user tries to login with his credentials"
            login() //credentials are taken from config file inside method
        then: "The user should be logged in and menu should be displayed"
            at MenuPage
    }
}


