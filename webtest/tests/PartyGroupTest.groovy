//Web-Test for PartyGroup(Company)
class PartyGroupTest extends LoginTest{
	/*
	 *  Create Company
	 */
	def testCompanyCreate() {
		tryLogin ( 'john', 'admin' )
        verifyText  'Import/Export'
        invoke( url:'partyGroup/list' )
        clickButton( label:'New' )   
        verifyText  'Create PartyGroup'
        clickButton 'Create'
        verifyText 'Name cannot be blank'
        setInputField( name: 'name', value: 'test' )
        setInputField( name: 'comment', value: 'WebTesting' )
        clickButton 'Create'
	}
	/*
	 *  Show, Edit And Delete of Company
	 */
	def testCompanyListNewDelete() {
		testCompanyCreate()
		clickLink   'List', description:'Back to list view'
		selectForm( name: 'selectForm_2' )
		clickButton 'Select', description:'Select Company'
		clickLink   'List', description:'Back to list view'
		group(description:'edit the one element') {
        showFirstElementDetails()
        clickButton 'Update'
        verifyText  'Show Company'
        clickLink   'List', description:'Back to list view'
		}
		group(description:'delete the only element') {
        showFirstElementDetails()
        clickButton 'Delete'
		}
	}
	/*
	 *  Show Company
	 */
	def showFirstElementDetails() {
		clickLink   'SIGMA', description:'go to detail view'
	}
}