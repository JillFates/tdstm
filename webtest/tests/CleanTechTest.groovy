class CleanTechTest extends MoveTechLoginTest {
	/*
	 *  Login into application using barcode.
	 */
    def testCleanTechLogin() {
        tryLoginMoveTech ( '', 'Invalid username format' )
        tryLoginMoveTech ( 'bt-27-34-s', 'Invalid username' )
        tryLoginMoveTech ( 'ct-88-34-s', 'Unknown move bundle' )
        tryLoginMoveTech ( 'ct-27-99-s', 'Unknown Logistics team' )
        tryLoginMoveTech ( 'ct-27-34-k', 'No assets assigned to team for move bundle' )
        tryLoginMoveTech ( 'abcdef', 'Invalid username format' )
        tryLoginMoveTech ( 'ct-27-34-s','Logistics' )          //proper barcode for login
        clickLink 'Log out'
	}
	
    /*
	 *  After login perform operations for myTask and Asset (common method for both moveTech and cleanTech)
	 */
	def cleanTechHomeMyTaskAndAsset() {
		clickLink('My Task')
		clickLink 'All (6)'
		verifyText(text:'C2A137')
		clickLink 'Todo (5)'
		verifyText( text:'C2A134' )
		setInputField( name: 'search', value: 'C2A134' )
		selectForm( name: 'bundleTeamAssetForm' )
		setInputField( name: 'search', value: 'C2A134' )
		invoke ( url: 'moveTech/cleaningAssetSearch?search=C2A134&bundle=27&team=34&project=24&location=s&tab=Todo' )
		verifyText( text: '105D74C CSMEDI' )
		clickButton( label: 'Cleaned' )
		verifyText ( text: 'You have not printed labels for this asset. Are you sure that you want to continue?' )
		verifyText( text: '105D74C CSMEDI' )
		clickLink( 'My Task' )
		verifyText( text: 'C2A134' )
		invoke ( url: 'moveTech/cleaningAssetSearch?search=C2A133&bundle=27&team=34&project=24&location=s&tab=Todo' )
		verifyText( text: '105C31D' )
		clickButton( label: 'Place on HOLD' )
		verifyText( text: 'Please enter note' )
		setSelectField( name: 'selectCmt', value: 'Device not powered down' )
		clickButton( label: 'Place on HOLD' )
		verifyText( text: 'C2A136' )		
		invoke ( url: 'moveTech/cleaningAssetSearch?search=C2A138&bundle=27&team=34&project=24&location=s&tab=Todo' )
		verifyText( text: 'AIX Console HMC2' )
		setSelectField( name: 'selectCmt', value: 'Device not powered down' )
		clickButton( label: 'Place on HOLD' )
		clickLink( 'My Task' )
		verifyText( text: 'C2A135' )
		invoke ( url: 'moveTech/cleaningAssetSearch?bundle=27&team=34&project=24&location=s&tab=Todo' )
		verifyText( text: 'C2A135' )
	}
	
	/*
	 *  cleanTech valid login for source
	 */
	def testForSource() {
		tryLoginMoveTech ( 'ct-27-34-s', 'Source' )
		cleanTechHomeMyTaskAndAsset()
	}
	
	/*
	 *  cleanTech valid login for target
	 */
	def testForTarget() {
		tryLoginMoveTech ( 'mt-27-34-t', 'Target' )
		cleanTechHomeMyTaskAndAsset()
	}
}