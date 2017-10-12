import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialType
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider

class CredentialTests extends IntegrationSpec {
    private ProjectTestHelper projectHelper = new ProjectTestHelper()

    void "1. Validate that deleting Project deletes the new Credential"() {
        given:
        Project project = projectHelper.createProjectWithDefaultBundle()

        when:
        project.addToProviders(new Provider(name: 'Test provider', comment: 'Test comment', description: 'Test description'))
        project.save(flush: true, failOnError: true)

        and:
        Provider provider = project.providers.first()

        and:
        project.addToCredentials(new Credential(
                type: CredentialType.PRODUCTION,
                status: CredentialStatus.ACTIVE,
                method: AuthenticationMethod.HTTP_BASIC,
                name: 'Test credential',
                salt: '{shablah}', accessKey: 'key', password: 'pwd',
                authenticationUrl: 'http://localhost', renewTokenUrl: 'http://localhost',
                expirationDate: new Date(),
                provider: provider))
        project.save(flush: true, failOnError: true)

        and:
        Credential credential = project.credentials.first()

        then:
        1 == project.credentials.size()
        null != credential.id

        and:
        project.delete()

        then:
        null == Credential.get(credential.id)
    }

    void "2. Validate that deleting Provider deletes the new Credential"() {
        given:
        Project project = projectHelper.createProjectWithDefaultBundle()
        Provider provider = new Provider(name: 'Test provider', comment: 'Test comment',
                description: 'Test description',
                project: project
        )

        when:
        provider.save(flush: true, failOnError: true)

        and:
        provider.addToCredentials(new Credential(
                type: CredentialType.PRODUCTION,
                status: CredentialStatus.ACTIVE,
                method: AuthenticationMethod.HTTP_BASIC,
                name: 'Test credential',
                salt: '{shablah}', accessKey: 'key', password: 'pwd',
                authenticationUrl: 'http://localhost', renewTokenUrl: 'http://localhost',
                expirationDate: new Date(),
                project: project))
        provider.save(flush: true, failOnError: true)

        and:
        Credential credential = provider.getCredentials().first()

        then:
        1 == provider.credentials.size()
        null != credential.id

        and:
        provider.delete()

        then:
        null == Credential.get(credential.id)
    }

}
