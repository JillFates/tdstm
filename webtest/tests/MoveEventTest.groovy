class MoveEventTest extends LoginTest {

    // Unlike unit tests, functional tests are sometimes sequence dependent.
    // Methods starting with 'test' will be run automatically in alphabetical order.
	// If you require a specific sequence, prefix the method name (following 'test') with a sequence
	// e.g. test001MoveEventListNewDelete

    
	def testMoveEventList() {
    	tryLogin ( 'john', 'admin' )
        
		verifyText  'Move Bundles'
        clickLink  'Move Bundles'
		
        verifyText  'MoveBundle List'
        verifyText  'Bundle 1'
        clickLink  'Bundle 1'
		
		verifyText  'Move Event'
        clickLink  'Move Event'
        verifyText  'Move Event List'
    }
    
    def testShowMoveEvent(){
    	testMoveEventList()
		verifyText 'Move Event 1'
		clickLink  'Move Event 1'
		
		verifyText 'Show Move Event'
		
		verifyText 'Project:'
		verifyText 'CS1 : Cedars-Sinai Move 1'
		
		verifyText 'Move Bundles:'
		verifyText 'Bundle 1'
		
		clickButton 'Edit'
    }
    
    def testEditMoveEvent(){
    	testShowMoveEvent()
		
		verifyText 'Edit Move Event'
		
		verifyText 'Move Bundles:'
		setCheckbox (name:"moveBundle", value:"28")
    	clickButton 'Update'
		
    	verifyText 'Show Move Event'
		
		verifyText 'Project:'
		verifyText 'CS1 : Cedars-Sinai Move 1'
		
		verifyText 'Move Bundles:'
		verifyText 'Bundle 2'
    }
    
    def testCreateMoveEvent(){
    	testMoveEventList()
		
		verifyText 'Create New'
		clickLink 'Create New'
		
		verifyText 'Create Move Event'
		
		setInputField( name: 'name', value: 'Test Move Even' )
		
        setSelectField( name: 'project.id', value: '25' )
		verifyText 'TW Bundle'
		
		setSelectField( name: 'project.id', value: '24' )
		verifyText 'Bundle 1'
		verifyText 'Bundle 2'
		verifyText 'Bundle 3'
		
    }
}