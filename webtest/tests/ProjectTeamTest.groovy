class ProjectTeamTest extends LoginTest {
	/*
	 *  Create Project Team
	 */
    def testProjectTeamCreate() {
    		tryLogin ( 'john', 'admin' )
            clickLink 'Team'
            verifyText  'Project Team List'

            clickButton 'New Project Team'
            verifyText  'Create Project Team'
            clickButton 'Create'
            verifyText 'Team Code cannot be blank'
            verifyText 'Team Name cannot be blank'
            
            setInputField( name: 'teamCode', value: 'TDS_1' )
            setInputField( name: 'name', value: 'TDS Team' )
            setSelectField( name: 'availableStaff', value: '1' )
            clickLink   'Assign >>'
            
            clickButton 'Create'
	}
	/*
	 *  Show, Edit And Delete of Project Team 
	 */
	def testProjectTeamShowEditAndDelete(){
			testProjectTeamCreate()
            verifyText  'Team Code must be unique'
            setInputField( name: 'teamCode', value: 'TDS_2' )
            clickButton 'Create'	
            verifyText  'Show Project Team'
            clickLink 'Team', description:'Detail page'
            
            group(description:'edit the one element') {
                showFirstElementDetails()
                clickButton 'Edit'
                verifyText  'Edit Project Team'
                setInputField( name: 'name', value: 'TDS_2 Team' )
                clickButton 'Update'
                verifyText  'Show Project Team'
                clickLink   'Team', description:'Back to list view'
            }

            group(description:'delete the only element') {
                showFirstElementDetails()
                clickButton 'Delete'
            }

    }

    def showFirstElementDetails() {
        clickLink   'TDS_2', description:'go to detail view'
    }
}