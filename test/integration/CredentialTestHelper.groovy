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
     * Create a default credential domain object
     * @param project
     * @param provider
     * @return
     */
    Credential createCredential(Project project, Provider provider) {
        return new Credential(
                type: CredentialType.PRODUCTION,
                status: CredentialStatus.ACTIVE,
                method: AuthenticationMethod.HTTP_BASIC,
                name: 'Test credential',
                salt: '{shablah}', accessKey: 'key', password: 'pwd',
                authenticationUrl: 'http://localhost', renewTokenUrl: 'http://localhost',
                expirationDate: new Date(),
                project: project,
                provider: provider
        )
    }

     Credential createAndSaveCredential(Project project, Provider provider) {
         createCredential(project, provider).save(flush: true, failOnError: true)
     }
}
