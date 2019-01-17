import com.tdsops.tm.enums.domain.SecurityRole
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import spock.lang.Specification
import test.helper.CredentialTestHelper
import test.helper.PersonTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper

@Integration
@Rollback
class CredentialIntegrationSpec extends Specification{

	ProjectService projectService
	SecurityService securityService

	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private ProviderTestHelper providerTestHelper = new ProviderTestHelper()
	private CredentialTestHelper credentialTestHelper = new CredentialTestHelper()
	private PersonTestHelper personHelper = new PersonTestHelper()

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
