package test.helper

import com.tdsops.common.security.AESCodec
import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.AuthenticationRequestMode
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
	private static final String SALT = 'abcdefd12345zxcy'

	/**
	 * Create a default Credential domain object
	 * @param project
	 * @param provider
	 * @return
	 */
	CredentialCommand createCredentialCO(Provider provider, String username = null, String password = null, String authenticationUrl = null, AuthenticationMethod authenticationMethod = null, String sessionName = null, AuthenticationRequestMode requestMode = null, String validationExpression = null) {
		return new CredentialCommand(
				environment: CredentialEnvironment.SANDBOX,
				requestMode: requestMode == null ? AuthenticationRequestMode.FORM_VARS : requestMode,
				httpMethod: CredentialHttpMethod.POST,
				status: CredentialStatus.ACTIVE,
				authenticationMethod: authenticationMethod == null ? AuthenticationMethod.BASIC_AUTH : authenticationMethod,
				sessionName: sessionName == null ? 'JSESSIONID' : sessionName,
				validationExpression: validationExpression == null ? '' : validationExpression,
				name: 'Test credential',
				username: username == null ? 'key' : username,
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
	Credential createAndSaveCredential(Project project, Provider provider, String username, String password, String authenticationUrl, AuthenticationMethod authenticationMethod, String sessionName, AuthenticationRequestMode requestMode, String validationExpression) {
		CredentialCommand credentialCO = createCredentialCO(provider, username, password, authenticationUrl, authenticationMethod, sessionName, requestMode, validationExpression)
		Credential credential = new Credential()
		credentialCO.populateDomain(credential)
		credential.project = project
		credential.salt = SALT
		credential.password = AESCodec.instance.encode(password, SALT)

		return credential.save(flush: true, failOnError: true)
	}
}
