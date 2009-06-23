class MoveTechLoginTest extends grails.util.WebTest {

    // Unlike unit tests, functional tests are sometimes sequence dependent.
    // Methods starting with 'test' will be run automatically in alphabetical order.
	// If you require a specific sequence, prefix the method name (following 'test') with a sequence
	// e.g. test001LoginListNewDelete
	
    /*
	 *  Common method to test login for MoveTech and CleanTech
	 */
    def tryLoginMoveTech ( def barcode, def checkText ) {
        invoke( url: 'moveTech/login' )
        verifyText  'Login'
        selectForm( name: 'loginForm' )
        setInputField( name: 'username', value: barcode )        
        clickButton( label: 'Login' )
        verifyText(text: checkText)
    }
}