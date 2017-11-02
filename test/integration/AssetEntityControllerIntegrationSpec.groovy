import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Shared

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.ui.Pagination


/**
 * Integration tests for the CommonController
*/
@TestFor(AssetEntityController)
class AssetEntityControllerIntegrationSpec extends Specification {

	@Shared
	PersonService personService
	//ProjectService projectService
	@Shared
	SecurityService securityService
	@Shared
	UserPreferenceService userPreferenceService

	static Person person
	static Project project
	static UserLogin adminUser
	static Person adminPerson
	
	static PersonTestHelper personHelper = new PersonTestHelper()
	static ProjectTestHelper projectHelper = new ProjectTestHelper()
	static AssetTestHelper assetHelper = new AssetTestHelper()

	/**
	 * Used to create a test project and user that is logged in
	 */
	def setupSpec() {
		personService = ApplicationContextHolder.getBean('personService')
		securityService = ApplicationContextHolder.getBean('securityService')
		userPreferenceService = ApplicationContextHolder.getBean('userPreferenceService')

		project = projectHelper.createProject()

		adminPerson = personHelper.createStaff(project.owner)
		assert adminPerson

		// projectService.addTeamMember(project, adminPerson, ['PROJ_MGR'])
		adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])
		assert adminUser
		assert adminUser.username

		// logs the admin user into the system
		securityService.assumeUserIdentity(adminUser.username, false)
		// println "Performed securityService.assumeUserIdentity(adminUser.username) with ${adminUser.username}"
		assert securityService.isLoggedIn()

		personService.addToProjectSecured(project, adminPerson)
	}

	def 'Test PaginationMethods.paginationMaxRowValue without user preferences'() {
		when: 'a number is not amoung the acceptable values'
			controller.params.max = '42'
		then: 'the default value should be returned'
			Pagination.MAX_DEFAULT == controller.paginationMaxRowValue('max')

		when: 'a non-numeric value is provided'	
			controller.params.max = 'try this one'
		then: 'the default value should be returned'
			Pagination.MAX_DEFAULT == controller.paginationMaxRowValue('max')
			
		when: 'a valid value is provided'	
			controller.params.max = Pagination.MAX_DEFAULT.toString()
		then: 'the expected value should be returned'
			Pagination.MAX_DEFAULT == controller.paginationMaxRowValue('max')
			
		when: 'the last value in the Pagination.MAX_OPTIONS is passed'	
			controller.params.max = Pagination.MAX_OPTIONS[-1].toString()
		then: 'the expected value should be returned'
			Pagination.MAX_OPTIONS[-1] == controller.paginationMaxRowValue('max')
	}

	def 'Test PaginationMethods.paginationMaxRowValue with user preferences'() {
		given: 'that there is no existing user preference'
			String pref = userPreferenceService.getPreference(PREF.MAX_ASSET_LIST)
			assert (! pref)

		when: 'paginationMaxRowValue is called with a bogus value'
			controller.params.max = 'fubar'
			controller.paginationMaxRowValue('max', PREF.MAX_ASSET_LIST)
		then: 'the user preference should be set to the default'
			Pagination.MAX_DEFAULT.toString() == userPreferenceService.getPreference(PREF.MAX_ASSET_LIST)

		when: 'paginationMaxRowValue is called with a valid value'
			String expectedValue = Pagination.MAX_OPTIONS[-1].toString()
			controller.params.max = expectedValue 
			controller.paginationMaxRowValue('max', PREF.MAX_ASSET_LIST)
		then: 'the user preference should be set to the new value'
			expectedValue == userPreferenceService.getPreference(PREF.MAX_ASSET_LIST)

		when: 'the user preference is removed'
			userPreferenceService.removePreference(PREF.MAX_ASSET_LIST)
		and: 'paginationMaxRowValue is called with a valid value'
			expectedValue == Pagination.MAX_OPTIONS[2].toString()
			controller.params.max = expectedValue
			controller.paginationMaxRowValue('max', PREF.MAX_ASSET_LIST)
		then: 'the user preference should be set to the value'
			expectedValue == userPreferenceService.getPreference(PREF.MAX_ASSET_LIST)

	}
}
