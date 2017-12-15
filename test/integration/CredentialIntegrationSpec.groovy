import com.tdsops.tm.enums.domain.SecurityRole
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.*
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import test.helper.CredentialTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper
import test.helper.PersonTestHelper

class CredentialIntegrationSpec extends IntegrationSpec {

    ProjectService projectService
    SecurityService securityService

    private ProjectTestHelper projectHelper = new ProjectTestHelper()
    private ProviderTestHelper providerTestHelper = new ProviderTestHelper()
    private CredentialTestHelper credentialTestHelper = new CredentialTestHelper()
    private PersonTestHelper personHelper = new PersonTestHelper()

    void "1. Validate that deleting Project deletes the new Credential"() {
        setup:
        Project project = projectHelper.createProjectWithDefaultBundle()
        Person adminPerson = personHelper.createStaff(project.owner)
        projectService.addTeamMember(project, adminPerson, ['PROJ_MGR'])

        UserLogin adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])
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
