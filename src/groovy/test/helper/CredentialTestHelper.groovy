package test.helper

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialType
import net.transitionmanager.command.CredentialCO
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
    CredentialCO createCredentialCO(Project project, Provider provider, String accessKey = null, String password = null, String authenticationUrl = null, AuthenticationMethod authenticationMethod = null) {
        return new CredentialCO(
                type: CredentialType.PRODUCTION,
                status: CredentialStatus.ACTIVE,
                method: authenticationMethod == null ? AuthenticationMethod.HTTP_BASIC : authenticationMethod,
                name: 'Test credential',
                accessKey: accessKey == null ? 'key' : accessKey,
                password: password == null ? 'pwd' : password,
                authenticationUrl: authenticationUrl == null ? 'http://localhost' : authenticationUrl,
                renewTokenUrl: 'http://localhost',
                expirationDate: new Date(),
                project: project,
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
        CredentialCO credentialCO = createCredentialCO(project, provider)
        Credential credential = new Credential()
        credentialCO.populateDomain(credential)
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
        CredentialCO credentialCO = createCredentialCO(project, provider, accessKey, password, authenticationUrl, authenticationMethod)
        Credential credential = new Credential()
        credentialCO.populateDomain(credential)
        credential.salt = '{sha}'

        return credential.save(flush: true, failOnError: true)
    }
}
