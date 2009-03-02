class AdminRoleValidationTest extends grails.util.WebTest {
	
	//	Login without Administrator role,
	def testLoginWithUserRole() {

        invoke(url: 'auth/login')

        selectForm(name:'loginForm')
        setInputField(name: 'username', value: 'ralph')
        setInputField(name: 'password', value: 'user')

        clickButton(label: 'Sign in')
        
        
        // click on Project label
        clickLink(label: 'Project')
        // invoke the user to access the secure page
        invoke( url: 'project/create' , description:'Tyring to access secure Pages' )

    }
	
}