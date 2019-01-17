import com.tdsops.common.ui.Pagination
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
/**
 * Integration tests for the CommonController
*/

@Integration
@Rollback
class AssetEntityControllerIntegrationSpec extends Specification {

	@Shared
	PersonService personService
	//ProjectService projectService
	@Shared
	SecurityService securityService
	@Shared
	UserPreferenceService userPreferenceService

	@Shared
	ProjectService projectService

	@Shared
	AssetEntityController controller = new AssetEntityController()

	static Person person
	static Project project
	static UserLogin adminUser
	static Person adminPerson

	static PersonTestHelper personHelper
	static ProjectTestHelper projectHelper
	static AssetTestHelper assetHelper

	/**
	 * Used to create a test project and user that is logged in
	 */
	def setup() {
		personHelper = new PersonTestHelper()
		projectHelper = new ProjectTestHelper()
		assetHelper = new AssetTestHelper()
		project = projectHelper.createProject()

		adminPerson = personHelper.createStaff(projectService.getOwner(project))
		assert adminPerson

		// projectService.addTeamMember(project, adminPerson, ['PROJ_MGR'])
		adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ROLE_ADMIN}"])
		assert adminUser
		assert adminUser.username

		// logs the admin user into the system
		securityService.assumeUserIdentity(adminUser.username, false)
		// println "Performed securityService.assumeUserIdentity(adminUser.username) with ${adminUser.username}"
		assert securityService.isLoggedIn()

		personService.addToProjectSecured(project, adminPerson)
	}

	@Unroll
	def 'Test PaginationMethods.paginationMaxRowValue without user preferences'() {
		when: 'setting the max param to a value'
			controller.params.max = maxValue
		then: 'the PaginationMethod.paginationMaxRowValue method should return the expect result'
			result == controller.paginationMaxRowValue('max')

		where:
			maxValue							  | result					
			'42'							   	  | Pagination.MAX_DEFAULT		// a number is not amoung the acceptable values
			'text'								  | Pagination.MAX_DEFAULT		// a non-numeric value is provided
			Pagination.MAX_DEFAULT.toString() 	  | Pagination.MAX_DEFAULT		// a valid value is provided
			Pagination.MAX_OPTIONS[-1].toString() | Pagination.MAX_OPTIONS[-1]	// the last value in the Pagination.MAX_OPTIONS is passed
			null								  | Pagination.MAX_DEFAULT		// a NULL value
			''									  | Pagination.MAX_DEFAULT		// a blank value
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

	def 'Test the PaginationMethods.paginationPage method'() {
		when: 'page parameter does not exist'			
		then: 'the page should be 1'
			1 == controller.paginationPage()

		when: 'page param value is null'
			controller.params.page = null	
		then: 'the page should be 1'
			1 == controller.paginationPage()

		when: 'page param value is non-numeric'
			controller.params.page = 'fubar'	
		then: 'the page should be 1'
			1 == controller.paginationPage()

		when: 'page param value is is negative number'
			controller.params.page = '-42'	
		then: 'the page should be 1'
			1 == controller.paginationPage()

		when: 'page param value is a positive number'
			controller.params.page = '42'	
		then: 'the page should match the parameter'
			42 == controller.paginationPage()

		when: 'page param value is a positive number and using a different name'
			controller.params.bestQB = '12'  // Tom Brady!	
		then: 'the page should match the parameter'
			12 == controller.paginationPage('bestQB')
	}

	def 'Kick the tires on the PaginationMethods.paginationRowOffset method'() {
        expect:
            result == controller.paginationRowOffset(page, rows)

        where:
            page    | rows  | result
            1       | 100   | 0
            0       | 100   | 0
            2       | 100   | 100
            null    | 100   | 0
            3       | 50    | 100
		
	}
}
