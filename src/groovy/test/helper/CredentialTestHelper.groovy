package test.helper

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialHttpMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialEnvironment
import net.transitionmanager.command.CredentialCommand
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider

/**
 * Use this class to create credential domain objects in your tests
 */
class CredentialTestHelper {

    /**
     * Create a default Credential domain object
     * @param project
     * @param provider
     * @return
     */
    CredentialCommand createCredentialCO(Provider provider, String accessKey = null, String password = null, String authenticationUrl = null, AuthenticationMethod authenticationMethod = null) {
        return new CredentialCommand(
                environment: CredentialEnvironment.PRODUCTION,
                httpMethod: CredentialHttpMethod.POST,
                status: CredentialStatus.ACTIVE,
                authenticationMethod: authenticationMethod == null ? AuthenticationMethod.BASIC_AUTH : authenticationMethod,
                name: 'Test credential',
                username: accessKey == null ? 'key' : accessKey,
                password: password == null ? 'pwd' : password,
                authenticationUrl: authenticationUrl == null ? 'http://localhost' : authenticationUrl,
                renewTokenUrl: 'http://localhost',
                provider: provider
        )
    }

    /**
     * Create and save a Credential domain object
     * @param project
     * @param provider
     * @return
     */
    Credential createAndSaveCredential(Project project, Provider provider) {
        CredentialCommand credentialCO = createCredentialCO(provider)
        Credential credential = new Credential()
        credentialCO.populateDomain(credential)
        credential.project = project
        credential.salt = '{sha}'

        return credential.save(flush: true, failOnError: true)
    }

    /**
     * Create and save a Credential domain object with provided authentication parameters
     * @param project
     * @param provider
     * @param accessKey
     * @param password
     * @param authenticationUrl
     * @return
     */
    Credential createAndSaveCredential(Project project, Provider provider, String accessKey, String password, String authenticationUrl, AuthenticationMethod authenticationMethod) {
        CredentialCommand credentialCO = createCredentialCO(provider, accessKey, password, authenticationUrl, authenticationMethod)
        Credential credential = new Credential()
        credentialCO.populateDomain(credential)
        credential.project = project
        credential.salt = '{sha}'

        return credential.save(flush: true, failOnError: true)
    }
}
