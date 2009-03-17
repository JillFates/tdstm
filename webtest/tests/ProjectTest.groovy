class ProjectTest extends LoginTest {
	/*
	 *  Create Project 
	 */
    def testProjectCreate() {
    		tryLogin ( 'john', 'admin' )
            clickLink 'Project'
            verifyText  'Project List'

            clickLink 'New Project'
            verifyText  'Create Project'
            clickButton 'Create'
            verifyText 'Project Code cannot be blank'
            verifyText 'Project Name cannot be blank'
            
            setInputField( name: 'projectCode', value: 'TDSTM' )
            setInputField( name: 'name', value: 'TDS' )
            setSelectField( name: 'projectPartner', value: '14' )
            setSelectField( name: 'projectManager', value: '1' )
            setSelectField( name: 'moveManager', value: '5' )
            
            clickButton 'Create'
	}
	/*
	 *  Show, Edit And Delete of Project  
	 */
	def testProjectShowEditAndDelete(){
			testProjectCreate()
            verifyText  'Project Code must be unique'
            setSelectField( name: 'projectPartner', value: '14' )
            setInputField( name: 'projectCode', value: 'TDSTM_NEW' )
            setSelectField( name: 'projectManager', value: '1' )
            setSelectField( name: 'moveManager', value: '5' )
            clickButton 'Create'	
            verifyText  'Show Project'
            clickLink 'Staff', description:'verify the staff list page loding time'
            clickLink 'Project', description:'Verify the Project show page loading time'
            
            group(description:'edit the one element') {
                showFirstElementDetails()
                clickButton 'Edit'
                verifyText  'Edit Project'
                setInputField( name: 'name', value: 'TDSTM_NEW' )
                clickButton 'Update'
                setSelectField( name: 'projectManager', value: '3' )
                setSelectField( name: 'moveManager', value: '6' )
                verifyText  'Show Project'
            }

            group(description:'delete the only element') {
                showFirstElementDetails()
                clickButton 'Delete'
            }

    }

    def showFirstElementDetails() {
        clickLink   'Project List', description:'go to List view'
        clickLink   'TDSTM_NEW', description:'go to detail view'
    }
}