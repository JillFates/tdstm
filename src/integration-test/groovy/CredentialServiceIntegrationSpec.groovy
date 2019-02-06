import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.AuthenticationRequestMode
import grails.core.GrailsApplication
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import grails.validation.ValidationException
import net.transitionmanager.command.CredentialCommand
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.service.CredentialService
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.ProjectRequiredException
import net.transitionmanager.service.SecurityService
import org.hibernate.SessionFactory
import spock.lang.Shared
import spock.lang.Specification
import test.helper.CredentialTestHelper
import test.helper.ProviderTestHelper

@Integration
@Rollback
class CredentialServiceIntegrationSpec extends Specification{
    CredentialService credentialService
    GrailsApplication grailsApplication
    SessionFactory    sessionFactory

    @Shared
    ProjectTestHelper projectTestHelper

    @Shared
    ProviderTestHelper providerTestHelper

    @Shared
    CredentialTestHelper credentialTestHelper

    def setup() {
        projectTestHelper = new ProjectTestHelper()
        providerTestHelper = new ProviderTestHelper()
        credentialTestHelper = new CredentialTestHelper()
        sessionFactory = grailsApplication.getMainContext().getBean('sessionFactory')
    }

    void '1. test create credential without project throws exception'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return null }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            CredentialCommand credentialCO = credentialTestHelper.createCredentialCO(provider)

        when: 'creating a credential with an invalid project'
            credentialService.create(credentialCO)
        then:
            ProjectRequiredException e = thrown()
    }

    // TODO : JPM 2/2018 : Enable once we solve the Mocking for SecurityService.getUserCurrentProject
    // void '2. test create credential without provider throws exception'() {
    //     setup:
    //         Project project = projectTestHelper.createProject()
    //         credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

    //     when: 'creating a Credential with no provider specified'
    //         credentialService.create(credentialCO)
    //     then: 'an InvalidParamException exception should be thrown'
    //         InvalidParamException e = thrown()
    //     and: 'the message should contain invalid provider'
    //         e.message ==~ /Invalid provider param.*/
    // }

    void '3. find credential id returns credential'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createCredential(project, provider, AuthenticationMethod.BASIC_AUTH, 'a@cookie:b', 'status code equal 200', 'http://b.ic', "", null, null)
            credential.save(flush: true, failOnError: true)
        when:
            def foundCredential = credentialService.findById(credential.id)
        then:
            foundCredential
            null != foundCredential.id
    }

    void '4. find credential by project returns a list of credentials'() {
        setup:
            Project project = projectTestHelper.createProject()
            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createCredential(project, provider, AuthenticationMethod.BASIC_AUTH, 'a@cookie:b', 'status code equal 200', 'http://b.ic', "", null, null)
            credential.save(flush: true, failOnError: true)
        when:
            def foundCredentials = credentialService.findAllByProject(project)
        then:
            foundCredentials
            3 == foundCredentials.size()
    }

    void '5. find credential by provider returns a list of credentials'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createCredential(project, provider, AuthenticationMethod.BASIC_AUTH, 'a@cookie:b', 'status code equal 200', 'http://b.ic', "", null, null)
            credential.save(flush: true, failOnError: true)
        when:
            def foundCredentials = credentialService.findAllByProvider(provider)
        then:
            foundCredentials
            1 == foundCredentials.size()
    }

    void '6. delete credential by id removes the credential from database'() {
        setup: ''
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createCredential(project, provider, AuthenticationMethod.BASIC_AUTH, 'a@cookie:b', 'status code equal 200', 'http://b.ic', "", null, null)
            credential.save(flush: true, failOnError: true)
            final Long id = credential.id
        when: 'the credential is deleted'
            credentialService.delete(id)
            sessionFactory.getCurrentSession().flush()
        and: 'then the findById is called'
            credentialService.findById(id)
        then: 'an EmptyResultException exception is expeced'
            thrown EmptyResultException
    }

    // TODO : JPM 2/2018 : Enable once we solve the Mocking for SecurityService.getUserCurrentProject
    // void '7. update an existing credential'() {
    //     setup:
    //         Project project = projectTestHelper.createProject()
    //         credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

    //         Provider provider = providerTestHelper.createProvider(project)
    //         Credential credential = credentialTestHelper.createAndSaveCredential(project, provider)
    //         final Long id = credential.id
        
    //     when: 'updating a Credential with a new name'
    //         final String newCredentialName = "***Credential name updated***"
    //         CredentialCommand credentialCO = new CredentialCommand()
    //         credentialCO.populateFromDomain(credential)
    //         credentialCO.name = newCredentialName
    //         credentialCO.version = credential.version
    //         Credential updatedCredential = credentialService.update(id, credentialCO)
    //     then: 'the returned credential should have the new name'
    //         newCredentialName == updatedCredential.name
    //     and: 'querying the database should return the new name'
    //         newCredentialName == credentialService.findById(id).name
    // }

    // TODO : JPM 2/2018 : Enable once we solve the Mocking for SecurityService.getUserCurrentProject
    // void '8. update an existing credential by passing an outdated version number throws DomainUpdateException'() {
    //     setup:
    //         Project project = projectTestHelper.createProject()
    //         credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

    //         Provider provider = providerTestHelper.createProvider(project)
    //         Credential credential = credentialTestHelper.createAndSaveCredential(project, provider)
    //         final Long id = credential.id

    //         CredentialCommand credentialCO = new CredentialCommand()
    //         credentialCO.populateFromDomain(credential)
    //         credentialCO.version = -1

    //     when: 'a Credential is updated with an outdated version number'
    //         credentialService.update(id, credentialCO)

    //     then: 'a DomainUpdateException shouold be thrown'
    //         DomainUpdateException e thrown
    //     and: 'the message should reflect that'
    //         'Someone else modified' == e.message

    // }

    void '9. saving credential with valid and invalid sessionName property value'() {
        setup:
            String sessionName = 'JSESSIONID'
            String validationExpression = 'body content equal "Welcome"'
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)

            Credential credential
            CredentialCommand credentialCO

        when: 'save credential with invalid sessionName'
            credentialCO = credentialTestHelper.createCredentialCO(provider, 'username',
                    'password', 'http://login.com', AuthenticationMethod.HEADER,
                    sessionName, AuthenticationRequestMode.FORM_VARS, validationExpression)
            credentialService.create(credentialCO)
        then: 'validation exception is thrown'
            thrown ValidationException
        when: 'save credential with valid sessionName'
            sessionName = 'JSESSIONID@cookie:JSESSIONID'
            credentialCO = credentialTestHelper.createCredentialCO(provider, 'username',
                'password', 'http://login.com', AuthenticationMethod.COOKIE,
                sessionName, AuthenticationRequestMode.FORM_VARS, validationExpression)
            credential = credentialService.create(credentialCO)
        then: 'credential gets saved without validation errors'
            credential
        when: 'save credential with invalid sessionName'
            sessionName = 'access_token @ cookie : JSESSIONID'
            credentialCO = credentialTestHelper.createCredentialCO(provider, 'username',
                'password', 'http://login.com', AuthenticationMethod.COOKIE,
                sessionName, AuthenticationRequestMode.FORM_VARS, validationExpression)
            credentialService.create(credentialCO)
        then: 'validation exception is thrown'
            thrown ValidationException
        when: 'save credential with invalid source name "headers" instead of "header"'
            sessionName = 'access_token@headers:access_token'
            credentialCO = credentialTestHelper.createCredentialCO(provider, 'username',
                'password', 'http://login.com', AuthenticationMethod.HEADER,
                sessionName, AuthenticationRequestMode.FORM_VARS, validationExpression)
            credentialService.create(credentialCO)
        then: 'validation exception is thrown'
            thrown ValidationException
    }
}
