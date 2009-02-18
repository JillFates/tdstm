class AuthControllerFunctionalTests extends functionaltestplugin.FunctionalTestCase {

    void testLogin() {
        get('/auth', 'login') {
            username "john"
            password "admin"
        }

        form("loginForm") {
            
            username = "john"
            password = "admin"
            click "submit"
        }

        assertContentContains "sign In"


    }
}




