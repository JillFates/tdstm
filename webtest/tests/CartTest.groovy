class CartTest extends LoginTest {

    //Test Cart For Non valid user.
    def testForNonCratUser () {
		 tryLogin ( 'ralph', 'user' )
		 verifyText  'Carts', description: ' Not a valid user to use Cart Tracking'
    }
    
    //Test Cart For valid user.
    def testForCratUser () {
		 tryLogin ( 'john', 'admin' )
		 //login success
		 verifyText  'Carts', description: ' valid user to use Cart Tracking'
		 clickLink   'Carts', description: ' view Carts list '
		 verifyText 'Cart Tracking'
		 //select form
		 selectForm(name:'cartTrackingForm')		 
		 clickLink   'Truck5'
		 selectForm(name:'changeTruckForm')
		 //change value of Truck
		 setSelectField( name: 'truck', value: 'Truck3' )
		 clickButton 'Update', description: ' Truck value is updated ' 
		 selectForm(name:'cartTrackingForm')
		 clickButton 'Refresh', description: ' Refresh page '
		 clickLink   'Truck4'
		 selectForm(name:'changeTruckForm')
		 clickButton 'Cancel', description: ' Truck div will be closed '
		 selectForm(name:'cartTrackingForm')
		 //change bundle
		 setSelectField( name: 'moveBundle', value: '28' )
		 clickButton 'All', description: ' Display completed Assets '
		 clickButton 'Remaining', description: ' Display non completed Assets '
    }
}