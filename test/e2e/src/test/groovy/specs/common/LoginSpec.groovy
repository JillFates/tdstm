package specs.common

import geb.spock.GebReportingSpec
import pages.common.LoginPage
import pages.common.MenuPage
import spock.lang.Stepwise

@Stepwise
class LoginSpec extends GebReportingSpec {
    static username
    static password

    def setupSpec() {
        //TODO put the following values on a property file
        username = "e2e_test_user"
        password = "e2e_password"
    }

    def loginTDS(){
        given:
        to LoginPage
        when:
        loginModule.login(username,password)
        then:
        at MenuPage
    }

}

