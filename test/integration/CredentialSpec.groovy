

import com.tdsops.common.security.AESCodec
import com.tdsops.tm.enums.domain.AuthenticationMethod as am
import com.tdsops.tm.enums.domain.CredentialEnvironment
import com.tdsops.tm.enums.domain.CredentialHttpMethod as chm
import com.tdsops.tm.enums.domain.AuthenticationRequestMode as arm
import com.tdsops.tm.enums.domain.CredentialStatus
import grails.test.mixin.TestFor
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import spock.lang.Specification
import test.helper.ProjectTestHelper
import org.apache.commons.lang3.RandomStringUtils
import test.helper.ProviderTestHelper

class CredentialSpec extends IntegrationSpec {

	ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	ProviderTestHelper providerTestHelper = new ProviderTestHelper()

	void 'Test the Session/Cookie name and ValidationExpression validation of Credential'() {
		expect:
		createTestCredential(method, sessionName, valExp, authUrl, renewUrl, httpMethod, requestMode).validate() == result

		where:
		method				| sessionName	| valExp				 | authUrl		 | renewUrl		| httpMethod 	| requestMode		| result
		am.BASIC_AUTH		| ''			| '' 					 | ''			 | '' 			| null			| null				| true		// HAPPY PATH
		am.BASIC_AUTH		| 'bogus'		| 'bogus'				 | ''			 | '' 			| null			| null				| true		// Still happy with bogus values that are unnecessary
		am.JWT				| ''			| '' 					 | 'http://b.ic' | 'http://p.ic'| null			| null				| true		// HAPPY PATH
		am.JWT				| 'bogus'		| 'bogus' 				 | 'http://b.ic' | 'http://p.ic'| null			| null				| true		// Still happy with bogus values that are unnecessary
		am.JWT				| ''			| '' 					 | 'http://b.ic' | 'http://b.ic'| null			| null				| false		// Authentication and renewUrl can not be the same
		am.JWT				| ''			| '' 					 | 'http://b.ic' | ''			| null			| null				| false		// Missing renewal URL
		am.JWT				| ''			| '' 					 | ''		 	 | 'http://b.ic'| null			| null				| false		// Missing authentication URL
		am.COOKIE			| 'a@cookie:b'	| 'status code equal 200'| 'http://b.ic' | ''			| chm.POST		| arm.BASIC_AUTH	| true		// HAPPY PATH
		am.COOKIE			| ''			| 'status code equal 200'| 'http://b.ic' | ''			| chm.POST		| arm.BASIC_AUTH	| false		// Missing sessionName
		am.COOKIE			| 'a@cookie:b'	| 'invalid'				 | 'http://b.ic' | ''			| chm.POST		| arm.BASIC_AUTH	| false		// Invalid validation expression
		am.COOKIE			| 'a@cookie:b'	| 'status code equal 200'| ''			 | ''			| chm.POST		| arm.BASIC_AUTH	| false		// Missing authentication URL
		am.COOKIE			| 'a@cookie:b'	| 'status code equal 200'| 'http://b.ic' | ''			| null			| arm.BASIC_AUTH	| false		// Missing httpMethod
		am.COOKIE			| 'a@cookie:b'	| 'status code equal 200'| 'http://b.ic' | ''			| chm.POST		| null				| false		// Missing requestMode
		am.HEADER			| 'a@header:b'	| 'status code equal 200'| 'http://b.ic' | ''			| chm.POST		| arm.BASIC_AUTH	| true		// HAPPY PATH
		am.HEADER			| ''			| 'status code equal 200'| 'http://b.ic' | ''			| chm.POST		| arm.BASIC_AUTH	| false		// Missing sessionName
		am.HEADER			| 'a@header:b'	| ''					 | 'http://b.ic' | ''			| chm.POST		| arm.BASIC_AUTH	| false		// Invalid validation expression
		am.HEADER			| 'a@header:b'	| 'status code equal 200'| ''			 | ''			| chm.POST		| arm.BASIC_AUTH	| false		// Missing authentication URL
		am.HEADER			| 'a@header:b'	| 'status code equal 200'| 'http://b.ic' | ''			| null			| arm.BASIC_AUTH	| false		// Missing httpMethod
		am.HEADER			| 'a@header:b'	| 'status code equal 200'| 'http://b.ic' | ''			| chm.POST		| null				| false		// Missing requestmode
	}

	/**
	 * Create a Credential domain object with the given parameters
	 * @param  authMethod
	 * @param  sessionName
	 * @param  validationExp
	 * @param  authUrl
	 * @param  renewUrl
	 * @param  httpMethod
	 * @param  requestMode
	 * @return  The Credential domain object
	 */
	private Credential createTestCredential(authMethod, sessionName, validationExp, authUrl, renewUrl, httpMethod, requestMode) {
		Project project = projectTestHelper.createProject()
		Provider provider = providerTestHelper.createProvider(project)

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
}