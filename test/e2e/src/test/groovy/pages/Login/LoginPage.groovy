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

    /**
     * for TM-9332
     * <p>This method will log the user in. The credentials can be whichever desired by adding the
     * -Dtm.creds.username=<username> -Dtm.creds.password=<password> parameters when running the test using command line. Else,
     * default credentials will be <b>'e2e_test_user'/'e2e_password'</b>. These credentials are in the testDataFile.txt file </p>
     * @return
     */
    def "login"() {
        Login log=new Login()
        String credentials=log.readCredentials()
        String usr = credentials.split(",")[0]
        String pass=credentials.split(",")[1]

        /**
         * The following two lines will use the credentials in te testData.txt file unless
         * different credentials are provided when executing.
         */
        username = System.properties['tm.creds.username']?: usr
        password =  System.properties['tm.creds.password'] ?: pass
        submitButton.click()
    }
}
