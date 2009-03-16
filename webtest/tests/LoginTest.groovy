class LoginTest extends grails.util.WebTest {

    // Unlike unit tests, functional tests are sometimes sequence dependent.
    // Methods starting with 'test' will be run automatically in alphabetical order.
	// If you require a specific sequence, prefix the method name (following 'test') with a sequence
	// e.g. test001LoginListNewDelete

    //Common method to test login
    def tryLogin ( def name, def password ) {

        invoke( url: 'auth/login' )

        verifyText  'Login'
        selectForm( name: 'loginForm' )
        setInputField( name: 'username', value: name )
        setInputField( name: 'password', value: password )
        clickButton( label: 'Sign in' )

    }
}