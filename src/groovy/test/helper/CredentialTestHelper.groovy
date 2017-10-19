package test.helper

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialType
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
    Credential createCredential(Project project, Provider provider, String accessKey = null, String password = null, String authenticationUrl = null, AuthenticationMethod authenticationMethod = null) {
        return new Credential(
                type: CredentialType.PRODUCTION,
                status: CredentialStatus.ACTIVE,
                method: authenticationMethod == null ? AuthenticationMethod.HTTP_BASIC : authenticationMethod,
                name: 'Test credential',
                salt: '{shablah}',
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
         return createCredential(project, provider).save(flush: true, failOnError: true)
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
         return createCredential(project, provider, accessKey, password, authenticationUrl, authenticationMethod).save(flush: true, failOnError: true)
     }
}
