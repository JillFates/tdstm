import grails.test.*
import com.tdssrc.grails.TimeUtil

import com.tdssrc.grails.GormUtil

class SecurityServiceTests extends GrailsUnitTestCase {
	
	def securityService    
	def newPerson
	def newUser
	def knownUser

	void testGetRoles() {

		def roles = securityService.getRoles(knownUser)
		println "Roles for $knownUser are: $roles"
		assertTrue 'Has roles', (roles && roles.size() > 0 ? true : false) 

		roles = securityService.getRoles(newUser)
		println "Roles for $newUser are: $roles"
		assertEquals 'New user shoud have zero roles', 0, (roles ? roles.size() : 0)
	}

	void testHasPermission() {

		assertTrue "User has permission", securityService.hasPermission(knownUser, 'CreateRecipe')
		assertFalse "Bogus permission", securityService.hasPermission(knownUser, 'BogusPermThatDoesNotExist')
		assertFalse "User does NOT has permission", securityService.hasPermission(newUser, 'CreateRecipe')

	}


    protected void setUp() {
        super.setUp()
		securityService = new SecurityService()

		// Create a new person and login that has no access to stuff
		newPerson = new Person(firstName:'Jack', lastName:'Rabbit', staffType:'Salary')
		assertTrue "Create new person", (newPerson.validate() && newPerson.save())
		newUser = new UserLogin(username:'xyzzy42', password:'guessit', person:newPerson, active:'Y', expiryDate:TimeUtil.nowGMT())
		assertTrue "Creating new UserLogin", (newUser.validate() && newUser.save() ? true : false) 

		knownUser = UserLogin.findByUsername('jmartin')
		assertNotNull 'Looking up known user jmartin', knownUser

	}

    protected void tearDown() {
        super.tearDown()
    }

}
