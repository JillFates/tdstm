package modules

import geb.Module

class LoginModule extends Module {

    static content = {
        username        { $("#usernameid") }
        password        { $("input", name:"password") }
        submitButton    { $("#submitButton") }
    }

    void login(String user, String pass) {
        username = user ?: System.properties['tm.creds.username']
        password = pass ?: System.properties['tm.creds.password']
        submitButton.click()
    }
}
