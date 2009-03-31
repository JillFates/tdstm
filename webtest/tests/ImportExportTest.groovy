class ImportExportTest extends LoginTest {
	/*
	 *  Test Asset Import 
	 */
    def testAssetImport() {
        tryLogin ( 'john', 'admin' )
        clickLink 'Import/Export'
        verifyText  'Asset Import'
            
        //Test for blank file input field.
        setSelectField( name: 'dataTransferSet', value: '1' )
        clickButton ( label:'Import Batch', description:'Test for File Import with blank file field' )
            
        //Test for selected file input field.
        setSelectField( name: 'dataTransferSet', value: '1' )
        setFileField( name: 'file', fileName: 'D:/serverListExample.xls' )
        clickButton ( label:'Import Batch', description:'Test for File Import with selected file' )
          
	}
}