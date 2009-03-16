class AuthControllerTest extends LoginTest { 

    //Attempted login with no username or password.
    def testUserBlankAuth() {

        tryLogin ( '', '' )
        verifyText  'Invalid username and/or password'
        
    }

    //Attempted login with invalid username and password.
    def testUserInvalidAuth() {

        tryLogin ( 'john', 'john' )
        verifyText  'Invalid username and/or password'
    }

    //Attempted login with valid username and password.
    def testUserValidAuth() {

        tryLogin ( 'john', 'admin' )
        verifyText  'Invalid username and/or password'
        verifyText  'New Project'

    }

    //Attempt accessing secure page without being logged in.  Should be redirected to login page.
    def testSecurePageWithoutLogin() {

        invoke( url: 'auth/login/home', description:'Trying to access secure Pages with out login' )

    }

    //Attempt accessing secure page while being logged in.  Should be redirected to insufficient rights warning page.
    def testSecurePageWithValidLogin() {

        tryLogin ( 'john', 'admin' )
        verifyText  'Import/Export'
        clickLink( label:'Assets' )

    }

    //Attempt logout after successful login, attempt to access secure page should redirect to login.
    def testLogOut() {

        tryLogin ( 'ralph', 'admin' )

        invoke( url: 'auth/signOut' , description:'Logout the Application' )
        
        invoke( url: 'auth/login/home' , description:'Tyring to access secure Pages after logout' )
    }
    
}