import com.tdsops.tm.enums.domain.SecurityRole
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.action.Credential
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.action.Provider
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import spock.lang.Shared
import spock.lang.Specification
import test.helper.CredentialTestHelper
import test.helper.PersonTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper

@Integration
@Rollback
class CredentialIntegrationSpec extends Specification {

	ProjectService  projectService
	SecurityService securityService

	@Shared
	ProjectTestHelper projectHelper

	@Shared
	ProviderTestHelper providerTestHelper

	@Shared
	CredentialTestHelper credentialTestHelper

	@Shared
	PersonTestHelper personHelper

	@Shared
	boolean initialized = false

	void setup() {
		if(!initialized) {
			projectHelper = new ProjectTestHelper()
			providerTestHelper = new ProviderTestHelper()
			credentialTestHelper = new CredentialTestHelper()
			personHelper = new PersonTestHelper()
			initialized = true
		}
	}

	void "1. Validate that deleting Project deletes the new Credential"() {
		setup:
			Project project = projectHelper.createProjectWithDefaultBundle()
			Person adminPerson = personHelper.createStaff(projectService.getOwner(project))
			projectService.addTeamMember(project, adminPerson, ['ROLE_PROJ_MGR'])

			UserLogin adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ROLE_ADMIN}"])
			securityService.assumeUserIdentity(adminUser.username, false)

			Provider provider = providerTestHelper.createProvider(project)
			credentialTestHelper.createAndSaveCredential(project, provider)

		when: 'project is deleted'
			projectService.deleteProject(project.id, true)

		and: 'finding all credentials by project'
			def credentials = Credential.findAllByProject(project)

		then: 'list of credentials by deleted project should be empty'
			[] == credentials
	}

	void "2. Validate that deleting Provider deletes the new Credential"() {
		given:
			Project project = projectHelper.createProjectWithDefaultBundle()
			Provider provider = providerTestHelper.createProvider(project)
			credentialTestHelper.createAndSaveCredential(project, provider)

		when: 'provider is deleted'
			provider.delete(flush: true)

		and: 'finding all credentials by deleted provider'
			def credentials = Credential.findAllByProvider(provider)

		then: 'list of credentials by deleted provider should be empty'
			[] == credentials
	}

}
