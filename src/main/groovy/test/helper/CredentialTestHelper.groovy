package test.helper

import com.tdsops.common.security.AESCodec
import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.AuthenticationRequestMode
import com.tdsops.tm.enums.domain.CredentialHttpMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialEnvironment
import grails.gorm.transactions.Transactional
import net.transitionmanager.command.CredentialCommand
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.commons.lang3.RandomStringUtils

/**
 * Use this class to create credential domain objects in your tests
 */
@Transactional
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
				validationExpression: validationExpression == null ? 'status code contains "200"' : validationExpression,
				name: RandomStringUtils.randomAlphanumeric(10),
				username: username == null ? 'username' : username,
				password: password == null ? 'password' : password,
				authenticationUrl: authenticationUrl == null ? 'http://localhost' : authenticationUrl,
				renewTokenUrl: 'http://localhost',
				provider: provider
		)
	}

	/**
	 * Create and return a Credential domain object
	 * @param project
	 * @param provider
	 * @param  authMethod
	 * @param  sessionName
	 * @param  validationExp
	 * @param  authUrl
	 * @param  renewUrl
	 * @param  httpMethod
	 * @param  requestMode
	 * @return  The Credential domain object
	 */
	Credential createCredential(Project project, Provider provider, authMethod, sessionName, validationExp, authUrl, renewUrl, httpMethod, requestMode) {

		def credential = new Credential()

		credential.username = 'username'
		credential.name = RandomStringUtils.randomAlphanumeric(10)
		credential.salt = AESCodec.instance.generateRandomSalt().substring(0,16)
		credential.password = AESCodec.instance.encode('password', credential.salt)
		credential.environment = CredentialEnvironment.SANDBOX
		credential.status = CredentialStatus.ACTIVE
		credential.project = project
		credential.provider = provider
		credential.authenticationMethod = authMethod
		credential.sessionName = sessionName
		credential.validationExpression = validationExp
		credential.authenticationUrl = authUrl
		credential.renewTokenUrl = renewUrl
		credential.httpMethod = httpMethod
		credential.requestMode = requestMode
		return credential
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
		credentialCO.populateDomain(credential, false, ['constraintsMap'])
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
