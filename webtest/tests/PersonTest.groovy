//WEb-Test for Staff
class PersonTest extends LoginTest {
	/*
	 *  Create Company-Staff
	 */
	def testStaffCreate() {
		tryLogin ( 'john', 'admin' )
        verifyText  'Import/Export'
        invoke( url:'person/list/17' )
        clickButton 'New'   
        verifyText  'Create Staff'
        clickButton 'Create'
        verifyText 'Person FirstName cannot be blank.'
        selectForm( name: 'createPersonForm' )
        setInputField( name: 'firstName', value: 'testName' )
        setSelectField( name: 'active', value: 'Y' )
		clickButton 'Create'
        
	}
	
	/*
	 *  Show, Edit And Delete of Company-Staff
	 */
	def testStaffListNewDelete() {
		testStaffCreate()
		group(description:'edit the one element') {
            showFirstElementDetails()
            clickButton 'Edit'
            verifyText  'Edit Person'
            selectForm( name: 'editForm' )
            setInputField( name: 'firstName', value: 'testName1' )
            setSelectField( name: 'active', value: 'Y' )
            clickButton 'Update'
            verifyText  'Staff List'
            clickLink   'Staff', description:'Back to list view'
		}
		group(description:'delete the only element') {
	        showFirstElementDetails()
	        clickButton 'Delete'
			}
	}
	/*
	 *  Show Company-Staff
	 */
	def showFirstElementDetails() {
		clickLink   'testName', description:'go to detail view'
	}
}