class ProjectStaffTest extends LoginTest {

    // Unlike unit tests, functional tests are sometimes sequence dependent.
    // Methods starting with 'test' will be run automatically in alphabetical order.
	// If you require a specific sequence, prefix the method name (following 'test') with a sequence
	// e.g. test001ProjectStaffListNewDelete

    def testProjectStaffListAddNew() {
    		tryLogin('admin','admin')
    		verifyText  'Invalid username and/or password'
    		setInputField( name: 'username', value: 'john' )
            setInputField( name: 'password', value: 'admin' )
            clickButton( label: 'Sign in' )
            verifyText  'Show Project'

            clickLink   'Staff'
            verifyText  'Project Staff List'
            clickButton 'Add'
            verifyText  'Add staff to project', description:'Detail page'
            
            selectForm( name: 'addSatffForm_0' )
            setSelectField( name: 'roleType', value: 'ADMIN' )
            clickButton 'Add', description:'Add staff to Project via Ajax service'
            
            selectForm( name: 'addSatffForm_1' )
            setSelectField( name: 'roleType', value: 'ADMIN' )
            clickButton 'Add', description:'Add staff to Project via Ajax service'
            
            clickButton 'Create New Staff', description:'Show Create for to create new staff to Project via Ajax service'
            verifyText  'Create New Staff', description:'Create page'
            
            selectForm( name: 'createForm' )
            setSelectField( name: 'company', value: '13' )
            setSelectField( name: 'roleType', value: 'PROJ_MGR' )
            setInputField( name: 'firstName', value: 'Raja' )
            setInputField( name: 'lastName', value: 'Reddy' )
            clickButton 'Create', description:'Add staff to Project via Ajax service'
            
            group(description:'edit the one element') {
                showFirstElementDetails()
                verifyText  'Edit Staff'
                setInputField( name: 'nickName', value: 'Raj' )
                clickButton 'Update'
                verifyText  'Project Staff List'
            }
    			
    		// click on  Administrator 
    		clickLink   'Administration'
    		verifyText 'Company'
    		
    		// click on company
    		clickLink 'Company'
    		selectForm( name:'selectForm_0')
    		clickButton 'Select'
    		
    		// get Staff list for TDS Company
    		clickLink 'Staff', description:'Staff List page'
    		verifyText 'Staff List'
    		verifyText 'Raja'
    }


    def showFirstElementDetails() {
        clickLink   'Raja Reddy', description:'go to detail view'
    }
}