import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.SecurityRole
import grails.test.mixin.TestFor
import net.transitionmanager.command.DataviewApiParamsCommand
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import spock.lang.Shared
import spock.lang.Specification

@TestFor(WsDataviewController)
class WsDataviewControllerIntegrationSpec extends Specification {

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

	void 'test can request an Dataview only by its ID'() {

		setup:
			controller.dataviewService = Mock(DataviewService) {
				query(_, _, _) >> { Project project, Dataview dataview, DataviewApiParamsCommand apiParamsCommand ->
					return [
						:
					]
				}
			}

		when:
			controller.request.method = 'GET'
			params.id = '1'
			controller.fetch(null, null)

		then:
			response.json == [:]


	}
}
