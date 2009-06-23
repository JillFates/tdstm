class MoveTechTest extends MoveTechLoginTest {
	/*
	 *  Login into application using barcode.
	 */
    def testMoveTechLogin() {
        tryLoginMoveTech ( '', 'Invalid username format' )
        tryLoginMoveTech ( 'bt-27-32-s', 'Invalid username' )
        tryLoginMoveTech ( 'mt-88-32-s', 'Unknown move bundle' )
        tryLoginMoveTech ( 'mt-27-99-s', 'Unknown move bundle team' )
        tryLoginMoveTech ( 'mt-27-32-k', 'No assets assigned to team for move bundle' )
        tryLoginMoveTech ( 'abcdef', 'Invalid username format' )
        tryLoginMoveTech ( 'mt-27-32-s', 'C2A136' )          //proper barcode for login
        clickLink 'Home'
        clickLink 'Log out'
	}
	
    /*
	 *  After login perform operations for myTask and Asset (common method for both moveTech and cleanTech)
	 */
	def moveTechHomeMyTaskAndAsset() {		
		clickLink 'All (6)'
		verifyText(text:'C2A137')
		clickLink 'Todo (4)'
		verifyText( text: 'C2A135' )
		setInputField( name: 'search', value: 'C2A135' )
		selectForm( name: 'bundleTeamAssetForm' )
		setInputField( name: 'search', value: 'C2A135' )
		invoke ( url: 'moveTech/assetSearch?search=C2A135&bundle=27&team=32&project=24&location=s&tab=Todo' )
		verifyText( text: 'AIX Console HMC3' )
		clickButton( label: 'Unracking' )
		invoke ( url: 'moveTech/assetSearch?search=C2A135&bundle=27&team=32&project=24&location=s&tab=Todo' )
		verifyText( text: 'KVM' )
		clickButton( label: 'Unracked' )
		invoke ( url: 'moveTech/assetSearch?search=C2A134&bundle=27&team=32&project=24&location=s&tab=Todo' )
		verifyText( text: '105D74C CSMEDI' )
		clickButton( label: 'Place on HOLD' )
		verifyText( text: 'Please enter note' )
		setSelectField( name: 'selectCmt', value: 'Device not powered down' )
		clickButton( label: 'Place on HOLD' )
		verifyText( text: 'C2A136' )
		clickLink 'Todo (3)'
		verifyText( text: 'C2A134' )
		clickLink 'All (6)'
		invoke ( url: 'moveTech/assetSearch?search=C2A137&bundle=27&team=32&project=24&location=s&tab=Todo' )
		verifyText( text: 'C2A137' )
		setSelectField( name: 'selectCmt', value: 'Device not powered down' )
		clickButton( label: 'Place on HOLD' )
		verifyText( text: 'MOVE_TECH does not have permission to change the State' )
	}
	
	/*
	 *  moveTech valid login for source
	 */
	def testForSource() {
		tryLoginMoveTech ( 'mt-27-32-s', 'C2A135' )
		moveTechHomeMyTaskAndAsset()
	}
	
	/*
	 *  moveTech valid login for target
	 */
	def testForTarget() {
		tryLoginMoveTech ( 'mt-27-33-t', 'C2A134' )
		moveTechHomeMyTaskAndAsset()
	}	
	
}