class AdminRoleValidationTest extends grails.util.WebTest {
	
	//	Login without Administrator role,
	def testLoginWithUserRole() {

        invoke(url:'auth/login')

        selectForm(name:'loginForm')
        setInputField(name:'username', value:'ralph')
        setInputField(name:'password', value:'user')

        clickButton(label: 'Sign in')
        
        // click on party Group label
        clickLink(label:'Party Group')
        
        // Trying to access a privileged page
        clickLink(label:'New PartyGroup')
        

    }
	
}