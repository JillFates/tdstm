class AdminRoleValidationTest extends LoginTest {
	
	//	Login without Administrator role,
	def testLoginWithUserRole() {

		tryLogin ( 'ralph', 'user' )
        
        // click on Project label
        clickLink(label: 'Project')
        verifyText  'Show Project'
        // invoke the user to access the secure page
        invoke( url: 'project/create' , description:'Tyring to access secure Pages' )
        verifyText  'You do not have permission to access this page'
    }
	
	/*
	 *  Test case for Session Expire
	 */
	 def testSessionExpire(){
		 tryLogin ( 'ralph', 'user' )
		 // logout the application to session expire
		 invoke( url: 'auth/signOut' , description:'Logout the Application' )
		 
		 // Check the Session Expire
		 invoke( url: 'person/index/9 ' , description:'Tyring to access secure Pages after Session Expire' )
        
		 invoke( url: 'project/show/15 ' , description:'Tyring to access secure Pages after Session Expire' )
	 }
}