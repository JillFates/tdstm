class AuthControllerTest extends grails.util.WebTest { 

    //Attempted login with no username or password.
    def testUserBlankAuth() {

        tryLogin ( '', '' )
        
    }

    //Attempted login with invalid username and password.
    def testUserInvalidAuth() {

        tryLogin ( 'john', 'john' )
        
    }

    //Attempted login with valid username and password.
    def testUserValidAuth() {

        tryLogin ( 'john', 'admin' )

    }

    //Attempt accessing secure page without being logged in.  Should be redirected to login page.
    def testSecurePageWithoutLogin() {

        invoke( url: 'auth/login/home', description:'Trying to access secure Pages with out login' )

    }

    //Attempt accessing secure page while being logged in.  Should be redirected to insufficient rights warning page.
    def testSecurePageWithValidLogin() {

        tryLogin ( 'john', 'admin' )

        clickLink( label:'Assets' )

    }

    //Attempt logout after successful login, attempt to access secure page should redirect to login.
    def testLogOut() {

        tryLogin ( 'ralph', 'admin' )

        invoke( url: 'auth/signOut' , description:'Logout the Application' )
        
        invoke( url: 'auth/login/home' , description:'Tyring to access secure Pages after logout' )
    }

    //Common method to test login
    def tryLogin ( def name, def password ) {

        invoke( url: 'auth/login' )

        selectForm( name: 'loginForm' )
        setInputField( name: 'username', value: name )
        setInputField( name: 'password', value: password )
        clickButton( label: 'Sign in' )

    }
    
}