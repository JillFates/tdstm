import grails.test.spock.IntegrationSpec
import grails.validation.ValidationException
import net.transitionmanager.command.CredentialCreateCO
import net.transitionmanager.command.CredentialUpdateCO
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.service.CredentialService
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.SecurityService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.SessionFactory
import test.helper.CredentialTestHelper
import test.helper.ProviderTestHelper

class CredentialServiceIntegrationSpec extends IntegrationSpec {
    CredentialService credentialService
    GrailsApplication grailsApplication
    SessionFactory sessionFactory

    ProjectTestHelper projectTestHelper = new ProjectTestHelper()
    ProviderTestHelper providerTestHelper = new ProviderTestHelper()
    CredentialTestHelper credentialTestHelper = new CredentialTestHelper()

    def setup() {
        sessionFactory = grailsApplication.getMainContext().getBean('sessionFactory')
    }

    void '1. test create credential without project throws exception'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return null }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            CredentialCreateCO credentialCO = credentialTestHelper.createCredentialCO(provider)

        when:
            credentialService.createCredential(credentialCO)

        then:
            ValidationException e = thrown()
            e.message.contains('field \'project\': rejected value [null]')
    }

    void '2. test create credential without provider throws exception'() {
        setup:
            CredentialCreateCO credentialCO = credentialTestHelper.createCredentialCO(null, null)

        when:
            credentialService.createCredential(credentialCO)

        then:
            AssertionError e = thrown()
            e.message ==~ /Invalid provider param.*/
    }

    void '3. find credential id returns credential'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createAndSaveCredential(project, provider)

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
            credentialTestHelper.createAndSaveCredential(project, provider)

        when:
            def foundCredentials = credentialService.findAllByProject(project)

        then:
            foundCredentials
            1 == foundCredentials.size()
    }

    void '5. find credential by provider returns a list of credentials'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            credentialTestHelper.createAndSaveCredential(project, provider)

        when:
            def foundCredentials = credentialService.findAllByProvider(provider)

        then:
            foundCredentials
            1 == foundCredentials.size()
    }

    void '6. delete credential by id removes the credential from database'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createAndSaveCredential(project, provider)
            final Long id = credential.id
        when:
            credentialService.deleteCredential(id)
            sessionFactory.getCurrentSession().flush()

        and:
            credentialService.findById(id)

        then:
            thrown EmptyResultException
    }

    void '7. update an existing credential'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createAndSaveCredential(project, provider)
            final Long id = credential.id
            final String newCredentialName = "***Credential name updated***"

            CredentialUpdateCO credentialCO = new CredentialUpdateCO()
            credentialCO.id = id
            credentialCO.populateFromDomain(credential)
            credentialCO.name = newCredentialName
            credentialCO.version = credential.version
        when:
            credentialService.updateCredential(credentialCO)

        then:
            credentialService.findById(id).name == newCredentialName
    }

    void '8. update an existing credential by passing an outdated version number throws DomainUpdateException'() {
        setup:
            Project project = projectTestHelper.createProject()
            credentialService.securityService = [getUserCurrentProject: { return project }] as SecurityService

            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createAndSaveCredential(project, provider)
            final Long id = credential.id

            CredentialUpdateCO credentialCO = new CredentialUpdateCO()
            credentialCO.populateFromDomain(credential)
            credentialCO.id = id
            credentialCO.version = -1
        when:
            credentialService.updateCredential(credentialCO)

        then:
            thrown DomainUpdateException
    }
}
