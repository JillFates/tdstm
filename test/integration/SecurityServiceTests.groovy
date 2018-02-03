import com.tdsops.common.exceptions.ServiceException
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdssrc.grails.TimeUtil
import groovy.time.TimeCategory
import net.transitionmanager.EmailDispatch
import net.transitionmanager.PasswordReset
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UnauthorizedException
import spock.lang.See
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.mop.ConfineMetaClassChanges

@Stepwise
class SecurityServiceTests extends Specification {

	// IOC
	SecurityService securityService

	private static final List<String> privRoles = ["${SecurityRole.ADMIN}", "${SecurityRole.EDITOR}", "${SecurityRole.USER}"]
	private static final List<String> userRole = ["${SecurityRole.USER}"]

	private PersonTestHelper personHelper = new PersonTestHelper()
	private Person privPerson
	private UserLogin privUser
	private Person unPrivPerson
	private UserLogin unPrivUser
	private Person userRolePerson
	private UserLogin userRoleUser

	// Helper methods to build up person/user accounts

	private void createPrivAccount() {
		// Create a new person and login that has a couple of security roles
		privPerson = personHelper.createPerson()
		privUser = personHelper.createUserLoginWithRoles(privPerson, privRoles)
	}

	private void createUnPrivAccount() {
		// Create a new person and login that has no security roles
		unPrivPerson = personHelper.createPerson()
		unPrivUser = personHelper.createUserLoginWithRoles(unPrivPerson, [])
	}

	private void createUserAccount() {
		// Create a new person and login that has ONLY the 'USER' security role
		userRolePerson = personHelper.createPerson()
		userRoleUser = personHelper.createUserLoginWithRoles(userRolePerson, userRole)
	}

	// Start of Specifications

	void '1 - Test the assignment and removal of roles to a person'() {

		// Please note that repeated adding and deleting of the same assignment in a single transaction
		// will cause a hibernate error with duplicate keys since the transaction hasn't been committed yet.
		// Therefore it is imperitive if you modify these tests further that you don't attempt this scenario.

		setup:
			createUnPrivAccount()
			List list, roles, rolesAdded
			def pr
			int count

		when: 'assigning the first role there should then be one role assigned to the person'
			pr = securityService.assignRoleCode(unPrivPerson, "${SecurityRole.USER}")
		then:
			pr != null
			securityService.getAssignedRoleCodes(unPrivPerson).size() == 1

		when: 'after assigning a second role there should be two assigned roles'
			pr = securityService.assignRoleCode(unPrivPerson, "${SecurityRole.EDITOR}")
			roles = securityService.getAssignedRoleCodes(unPrivPerson)
		then:
			pr != null
			roles == ["${SecurityRole.EDITOR}", "${SecurityRole.USER}"]

		when: 'assigning the same role twice it should make not difference'
			pr = securityService.assignRoleCode(unPrivPerson, "${SecurityRole.EDITOR}")
			list = securityService.getAssignedRoleCodes(unPrivPerson)
		then:
			pr != null
			list.size() == 2
			list == ["${SecurityRole.EDITOR}", "${SecurityRole.USER}"]

		when: 'unassigning a role from the user should return a 1 indicating that it was deleted'
			count = securityService.unassignRoleCodes(unPrivPerson, ["${SecurityRole.EDITOR}"])
		then:
			count == 1
		and: 'the person should only have one role (USER) remaining if I did the math correctly'
			securityService.getAssignedRoleCodes(unPrivPerson) == ["${SecurityRole.USER}"]

		when: 'adding roles in bulk it should not complain and should then return the complete list of roles'
			rolesAdded = securityService.assignRoleCodes(unPrivPerson, ["${SecurityRole.SUPERVISOR}", "${SecurityRole.ADMIN}"])
		then:
			rolesAdded.size() == 2
		and: 'the list should have the 3 unPrivPerson roles and the list should be ordered by the security level descending'
			securityService.getAssignedRoleCodes(unPrivPerson) == ["${SecurityRole.ADMIN}", "${SecurityRole.SUPERVISOR}", "${SecurityRole.USER}"]

		when: 'unassigning roles in bulk'
			count = securityService.unassignRoleCodes(unPrivPerson, ["${SecurityRole.SUPERVISOR}", "${SecurityRole.USER}"])
		then: 'it should return that 2 were removed'
			count == 2
		and: 'there should only be one role left and while we are at it we will look it up by the UserLogin this time'
		securityService.getAssignedRoleCodes(unPrivUser) == ["${SecurityRole.ADMIN}"]

		when: 'passing in an invalid role code'
			securityService.assignRoleCode(unPrivPerson, 'BOGUS')
		then: 'an exception should be thrown'
			thrown InvalidParamException

		when: 'passing in an non-security type role code (e.g. TEAM:PROJ_MGR)'
			securityService.assignRoleCode(unPrivPerson, 'PROJ_MGR')
		then: 'an exception should be thrown'
			thrown InvalidParamException
	}

	void '2 - Test the getMaxAssignedRole method to see if it is behaving as it should'() {
		setup:
			createPrivAccount()

		when: 'starting this test the initial list of roles for the privPerson it should match the privRoles defined up top'
			List roles = securityService.getAssignedRoleCodes(privPerson)
		then:
			roles == privRoles
		and: 'the top privileged role should be ADMIN'
		"${SecurityRole.ADMIN}" == securityService.getMaxAssignedRole(privPerson)?.id

		when: 'deleting the ADMIN role we can expect that the next role up will be EDITOR'
			securityService.unassignRoleCodes(privPerson, ["${SecurityRole.ADMIN}"])
		then:
		"${SecurityRole.EDITOR}" == securityService.getMaxAssignedRole(privPerson)?.id

		when: 'deleting the remaining roles the next call to getMaxAssignedRole should just return a null'
			securityService.unassignRoleCodes(privPerson, privRoles)
		then:
			null == securityService.getMaxAssignedRole(privPerson)?.id
	}

	void '3 - Test the max role to see that ADMIN is still the top dog'() {
		expect:
		"${SecurityRole.ADMIN}" == securityService.getAllRoles()[0]?.id
	}

	void '4 - Test the getAssignableRoles and getAssignableRoleCodes method'() {
		setup:
			List expectedRoles = ["${SecurityRole.SUPERVISOR}", "${SecurityRole.EDITOR}", "${SecurityRole.USER}"]
			List results

		when: 'creating the unprivileged account that has no roles one would expect that they can not assign roles'
			createUnPrivAccount()
		then: 'the individual has the SUPERVISOR role'
			0 == securityService.getAssignableRoles(unPrivPerson).size()

		when: 'the unprivileged account is assigned a role but still does not have necessary permission'
			assert securityService.assignRoleCode(unPrivPerson, "${SecurityRole.SUPERVISOR}")
		then: 'it should fail to get the roles'
			!securityService.getAssignableRoles(unPrivPerson)

		when: 'the unprivileged account is assigned ADMIN role'
			assert securityService.assignRoleCode(unPrivPerson, "${SecurityRole.ADMIN}")
			results = securityService.getAssignableRoles(unPrivPerson)
		then: 'number of roles returned will have increased'
			results.size() >= 6
	}

	void '5 - Test the getAllRoles and getAllRoleCodes methods'() {
		setup:
			List list

		when: 'calling getAllRoles without the maxLevel parameter'
			list = securityService.getAllRoles()
		then: 'it should return all roles'
			list.size() >= 6
			list.find { it.id == "${SecurityRole.ADMIN}" }
			list.find { it.id == "${SecurityRole.USER}" }

		when: 'calling getAllRoles with a the maxLevel of 30 getAllRoles'
			list = securityService.getAllRoles(30)
		then: 'it should return 3 roles'
			list.size() == 3
			list*.id == ["${SecurityRole.SUPERVISOR}", "${SecurityRole.EDITOR}", "${SecurityRole.USER}"]
	}

	void '6 - Test the hasPermission for different user scenarios'() {
		setup:
			createPrivAccount()
			createUnPrivAccount()
			assert securityService.assignRoleCode(unPrivPerson, "${SecurityRole.USER}")
			String privPerm = 'ApplicationRestart'

		expect: 'that calling hasPermission for privileged user with the ADMIN role returns true'
			securityService.hasPermission(privUser, privPerm)
		and: 'that a bogus permission code will return false'
			!securityService.hasPermission(privUser, 'BogusPermThatDoesNotExist')
		and: 'that a user with minimal privileges will not have the right to higher privileged permissions'
			!securityService.hasPermission(unPrivUser, privPerm)

		when: 'called with a null UserLogin'
			securityService.hasPermission(null, privPerm)
		then: 'it should throw an exception'
			thrown InvalidParamException

		when: 'called with a null permission code'
			securityService.hasPermission(unPrivUser, null)
		then: 'it should throw an exception'
			thrown InvalidParamException
	}

	//
	// TODO : JPM 4/2016 : Tried to implement some meta programming to test inside method
	// net.transitionmanager.service.SecurityService.reportViolation however after running the tests the old method is not restored as advertised
	// See http://blog.jdriven.com/2014/11/spock-using-confinemetaclasschanges-using-metaclass-mocking/ and
	// http://mrhaki.blogspot.com/2015/09/spocklight-undo-metaclass-changes.html
	@ConfineMetaClassChanges([SecurityService])
	void '7 - Test the hasPermission calls reportViolation if user does not have permission'() {
		setup:
			createUnPrivAccount()
			String privPerm = 'ApplicationRestart'

			// Use this command to figure out the method signature we want to override
			println "net.transitionmanager.service.SecurityService.reportViolation method signatures:\n" +
				securityService.metaClass.methods.findAll { it.name == "reportViolation" }.join("\n")

			// Save off the old method
			def oldMethod = securityService.metaClass.getMetaMethod("reportViolation", [String, UserLogin] as Class[])

			// Do some meta programming here to intercept the reportViolation method
			int reportViolationCalled = 0

			// To test this, uncomment the next
			// securityService.metaClass.reportViolation = { msg, username -> reportViolationCalled++ }

		when: 'the user checks for an unpermitted permission'
			securityService.hasPermission(unPrivUser, privPerm, true)
		then: 'it should invoke the inner reportViolation method'
			// If the metaClass method overide is done then this works but leave the method broken
			reportViolationCalled == 0

		cleanup:
			reportViolationCalled = 0
			// Restore the reportViolation method on the service some how. Docs state to set the metaClass to null
			// but that didn't work.
			// The @ConfineMetaClassChanges annotation is suppose to work as well but nada...
			// securityService.metaClass = null
	}

	void '8 - Test the that the password strength logic as tight as Fort Knox'() {
		setup:
			String username = 'UserName7!'

		expect: """ The test should validate that the password meets the following criteria:
			- at least 8 characters in length
			- at least any three of the four character types
			    - lowercase char
			    - uppercase char
			    - digit
			    - non alpha/digit (e.g. !,%^&)
			- does not contain the username within it """

			securityService.validPasswordStrength(username, password) == result
		where:
			password       | result
			'Password!'    | true
			'password7!'   | true
			'Password7'    | true
			'PASSWORD7!'   | true
			'Password77!'  | true
			'password'     | false
			'Password'     | false
			'password7'    | false
			'password!'    | false
			'UserName7!'   | false
			'*UserName7!'  | false
			'UserName7!*'  | false
			'*UserName7!*' | false
			'PASSWORD'     | false
			'PASSWORD7'    | false
			'pswd'         | false
			'PSWD'         | false
			'!!!!!!!!'     | false
			'88888888'     | false
			'aaaaaaaa'     | false
			'AAAAAAAA'     | false
			'pswd7'        | false
			'pswd!'        | false
			'Pswd!'        | false
			'pswd7!'       | false
			'PSWD7!'       | false
			'Pswd7!'       | false
	}

	def '9 - Test the keepWhichOnMerge method'() {
		when: 'the From user has lastLogin and To does not'
			Person fromPerson = new Person(active:'Y')
			Person toPerson = new Person(active:'Y')
			UserLogin fromUser = new UserLogin(lastLogin:new Date(), person:fromPerson)
			UserLogin toUser = new UserLogin(person:toPerson)
		then: 'the From user should be used and vice versa'
			securityService.keepWhichOnMerge(fromUser, toUser) == 'from'
			securityService.keepWhichOnMerge(toUser, fromUser) == 'to'

		when: 'both users have lastLogin values set'
			Date earlyDate, laterDate
			use(TimeCategory) {
				earlyDate = new Date() + 30.days
				laterDate = earlyDate + 1.month
			}
			fromUser.lastLogin = earlyDate
			toUser.lastLogin = laterDate
		then: 'the one with the most recent should be selected'
			securityService.keepWhichOnMerge(fromUser, toUser) == 'to'
			securityService.keepWhichOnMerge(toUser, fromUser) == 'from'

		when: 'both users have no lastLogin the next check is to look at active accounts'
			fromUser.lastLogin = null
			toUser.lastLogin = null
			fromUser.expiryDate = laterDate
			fromUser.active = 'Y'
			toUser.active = 'N'
		then: 'when the one is active and the other not, the active one should be choosen'
			securityService.keepWhichOnMerge(fromUser, toUser) == 'from'
			securityService.keepWhichOnMerge(toUser, fromUser) == 'to'

		when: 'both users are active then the next check is based on createDate'
			toUser.active = 'Y'
			toUser.expiryDate = laterDate
			fromUser.createdDate = earlyDate
			toUser.createdDate = laterDate
		then: 'the account with the most recent date should be choosen'
			securityService.keepWhichOnMerge(fromUser, toUser) == 'to'
			securityService.keepWhichOnMerge(toUser, fromUser) == 'from'
	}

	def '10. Test the assumeUserIdentity method'() {
		setup: 'a valid username'
			createPrivAccount()
			String username = privUser.username
		when: 'calling assumeUserIdentity with preventWebInvocation=false'
			securityService.assumeUserIdentity(username, false)
		then: 'calling isLoggedIn() should be true'
			securityService.isLoggedIn()

		when: 'calling assumeUserIdentity with valid username and preventWebInvocation=true'
			securityService.assumeUserIdentity(username, true)
		then: 'an exception should be thrown'
			thrown UnauthorizedException

		when: 'calling assumeUserIdentity with an INVALID username'
			username = 'someBogusUsernameThatCanNotPossiblyExist'
			securityService.assumeUserIdentity(username)
		then: 'an exception should be thrown'
			thrown RuntimeException
	}

	def '11. Test hasPermission() with nonexistent permission should throw a RuntimeException'() {
		when: 'called with an inexistent permission'
			securityService.hasPermission(Permission.MoveEventView)
		then: 'it should throw an exception'
			thrown RuntimeException
	}

	@See("https://support.transitionmanager.com/browse/TM-6346")
	def '12. Test Password Reset expiration date'() {
		setup: 'a valid username and Reset Data'
			createPrivAccount()
			String token = "SomeToken"
			String ipAddress = "127.0.0.1"
			EmailDispatch ed = null //Not really needed
			PasswordResetType resetType = PasswordResetType.WELCOME
			long tokenTTL = securityService.accountActivationTTL
			long FIVE_SECONDS = 5*1000

		when: "a Pasword reset is created"
			PasswordReset pr = securityService.createPasswordReset(token, ipAddress, privUser, privPerson, ed, resetType)
			Date expTime = new Date(TimeUtil.nowGMT().time + tokenTTL)

		then: "the PaswordReset whould expire between 5 seconds of the manually calculated expiration Time"
			((expTime.time - FIVE_SECONDS) <= pr.expiresAfter.time) && (pr.expiresAfter.time <= (expTime.time + FIVE_SECONDS) )
	}

	def '13. Test Password Reset valid before TTL'() {
		setup: 'a valid username and Reset Data'
			createPrivAccount()
			//init values for user configuration
			privUser.expiryDate = new Date(new Date().time + 24 * 60 * 60 * 1000) //24Hrs
			privUser.active = 'Y'
			privPerson.active = 'Y'
			String token = "SomeToken"
			String ipAddress = "127.0.0.1"
			EmailDispatch ed = null //Not really needed
			PasswordResetType resetType = PasswordResetType.WELCOME
			long tokenTTL = securityService.accountActivationTTL

		when: "a Pasword reset is created"
			PasswordReset pr = securityService.createPasswordReset(token, ipAddress, privUser, privPerson, ed, resetType)
		then: "the PaswordReset should be valid"
			PasswordReset pr2 = securityService.validateToken(token)
			pr2 != null

	}

	def '14. Test PasswordReset invalid if an already expired TTL is selected'() {
		setup: 'a valid username and Reset Data'
			long THREE_SECONDS_AGO = -3000
			createPrivAccount()
			//init values for user configuration
			privUser.expiryDate = new Date(new Date().time + 24 * 60 * 60 * 1000) //24Hrs
			privUser.active = 'Y'
			privPerson.active = 'Y'
			securityService.setAccountActivationTTL(THREE_SECONDS_AGO)
			String token = "SomeToken"
			String ipAddress = "127.0.0.1"
			EmailDispatch ed = null //Not really needed
			PasswordResetType resetType = PasswordResetType.WELCOME
			long tokenTTL = securityService.accountActivationTTL

		when: 'an PasswordReset is created with and expired date'
			securityService.createPasswordReset(token, ipAddress, privUser, privPerson, ed, resetType)
		and: 'validating the token is attempted'
			securityService.validateToken(token)
		then: 'an exception should be thrown'
			ServiceException ex = thrown()
		and: 'the exception has message identify that the token has expired'
			ex.message.startsWith("The password reset token has expired")
	}

	def '15. Test Password Reset expiration Cron cleanup'() {
		setup: 'a valid username and Reset Data'
			long THREE_SECONDS_AGO = -3000
			createPrivAccount()
			//init values for user configuration
			privUser.expiryDate = new Date(new Date().time + 24 * 60 * 60 * 1000) //24Hrs
			privUser.active = 'Y'
			privPerson.active = 'Y'
			securityService.setAccountActivationTTL(THREE_SECONDS_AGO)
			String token = "SomeToken"
			String ipAddress = "127.0.0.1"
			EmailDispatch ed = null // Not really needed
			PasswordResetType resetType = PasswordResetType.WELCOME
			int initialNumOfPending = PasswordReset.findAllByStatus("PENDING").size()

		when: 'a PasswordReset is created with a past-due expiry date'
			PasswordReset pr = securityService.createPasswordReset(token, ipAddress, privUser, privPerson, ed, resetType)
		and:  'we get the list the of PENDING PasswordReset records'
			List<PasswordReset> lista = PasswordReset.findAllByStatus("PENDING") ?: []
		then: 'the list should contain an additional PasswordReset record'
			lista.size() == (initialNumOfPending + 1)

		when: 'the cleanupPasswordReset method is called'
			securityService.cleanupPasswordReset()
			List<PasswordReset> list = PasswordReset.findAllByStatus("PENDING") ?: []
		then: 'the expired PasswordReset record should be removed'
			list.size() <= initialNumOfPending
	}

    def '16. Calling currentUserPermissionAsMap as an unauthenticated user' () {
        given:'a user whom is NOT authenticated'
        	securityService.logoutCurrentUser()
        when:'the securityService.currentUserPermissionAsMap method is called'
        	Map permissions = securityService.currentUserPermissionMap()
        then:'an empty map should be returned.'
        	permissions.isEmpty()
    }

    def '17. Calling currentUserPermissionAsMap as an authenticated and under privileged user' () {
        given:'a user whom is authenticated and has only been assigned the User role'
        	createUserAccount()
        	securityService.assumeUserIdentity(userRoleUser.username, false)
        when:'the securityService.currentUserPermissionAsMap method is called'
        	Map permissions = securityService.currentUserPermissionMap()
		then:'a map should be returned containing multiple elements'
        	!permissions.isEmpty()
        and:'the map should contain the Permission.UserGeneralAccess permission'
        	permissions.containsKey(Permission.UserGeneralAccess)
        and:'the map should NOT contain the Permission.UserDelete permission'
        	!permissions.containsKey(Permission.UserDelete)
    }

    def '18. Calling currentUserPermissionAsMap as an authenticated and privileged user' () {
		given:'a user whom is authenticated has been assigned the ADMIN role'
        	createPrivAccount()
        	securityService.assumeUserIdentity(privUser.username, false)
        when:'the securityService.currentUserPermissionAsMap method is called'
        	Map permissions = securityService.currentUserPermissionMap()
        then:'a map should be returned containing multiple elements'
        	!permissions.isEmpty()
        and:'the map should contain the Permission.UserGeneralAccess permission'
        	permissions.containsKey(Permission.UserGeneralAccess)
        and:'the map should also contain the Permission.UserDelete permission'
        	permissions.containsKey(Permission.UserDelete)
    }
}