class ApplicationTest extends LoginTest {
	


	
//  Test Ajax Overlay for Create.
    def testApplicationCreate() {

        tryLogin ( 'john', 'admin' )
        verifyText  'Import/Export'
        invoke( url:'application/list/16' )
        clickButton( label:'Create a New Application' )   
        verifyText  'Create Application'
        clickButton 'Create'
        verifyText 'AppCode cannot be blank'
        
        setInputField( name: 'appCode', value: 'CSMCARM' )
         setInputField( name: 'name', value: 'test' )
        setSelectField( name: 'environment', value: 'Production' )
        clickButton( label:'Create' )        
           
       
    }
    /*
	 *  Show, Edit And Delete of Application
	 */
    def testApplicationShowEditAndDelete(){
    	testApplicationCreate()
        setInputField( name: 'appCode', value: 'CSMCARM1' )
        setSelectField( name: 'environment', value: 'Production' )
        clickButton 'Create'	
        
        
        group(description:'edit the one element') {
            showFirstElementDetails()
            clickButton 'Edit'
            verifyText  'Edit Application'
            setInputField( name: 'applicationCoded', value: 'CSMCARM1' )
            setInputField( name: 'named', value: 'test' )
            setSelectField( name: 'environmentd', value: 'Production' )
            clickButton 'Update Application'
            verifyText  'Show Application'
            clickLink   'Applications', description:'Back to list view'
        }

        group(description:'delete the only element') {
            showFirstElementDetails()
            clickButton 'Delete'
        }

}
    def showFirstElementDetails() {
        clickLink   '27', description:'go to detail view'
    }

}