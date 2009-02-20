class AuthControllerTest extends grails.util.WebTest { 

    //Attempted login with no username or password.
    def testUserBlankAuth() {

        invoke( url: 'auth/login' )
        
        selectForm(name:'loginForm')
        setInputField(name:'username', value:'')
        setInputField(name:'password', value:'')

        clickButton( label: 'Sign in' )
        
    }

    //Attempted login with invalid username and password.
    def testUserInvalidAuth() {

        invoke( url: 'auth/login' )
        
        selectForm(name:'loginForm')
        setInputField(name: 'username', value: 'john')
        setInputField(name: 'password', value: 'john')

        clickButton( label: 'Sign in' )
        
    }

    //Attempted login with valid username and password.
    def testUserValidAuth() {

        invoke( url: 'auth/login' )

        selectForm(name: 'loginForm')
        setInputField(name: 'username', value:'john')
        setInputField(name: 'password', value:'admin')

        clickButton(label: 'Sign in')

    }

    //Attempt accessing secure page without being logged in.  Should be redirected to login page.
    def testSecurePageWithoutLogin() {

        invoke( url: 'auth/login/home' )

    }

    //Attempt accessing secure page while being logged in.  Should be redirected to insufficient rights warning page.
    def testSecurePageWithValidLogin() {

        invoke(url: 'auth/login')

        selectForm(name: 'loginForm')
        setInputField(name: 'username', value:'john')
        setInputField(name: 'password', value:'admin')

        clickButton(label: 'Sign in')
        
        invoke( url: 'party/list' )

    }

    //Attempt logout after successful login, attempt to access secure page should redirect to login.
    def testLogOut() {

        invoke( url: 'auth/login' )

        selectForm( name: 'loginForm' )
        setInputField( name: 'username', value: 'john' )
        setInputField( name: 'password', value: 'admin' )

        clickButton(label: 'Sign in')

        invoke( url: 'auth/signOut' )
        
        invoke( url: 'auth/login/home' )
    }
    
}