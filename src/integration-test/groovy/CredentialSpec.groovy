import com.tdsops.tm.enums.domain.AuthenticationMethod as am
import com.tdsops.tm.enums.domain.AuthenticationRequestMode as arm
import com.tdsops.tm.enums.domain.CredentialHttpMethod as chm
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import spock.lang.Specification
import test.helper.CredentialTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper

@Integration
@Rollback
class CredentialSpec extends  Specification{

	ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	ProviderTestHelper providerTestHelper = new ProviderTestHelper()
	CredentialTestHelper credentialTestHelper = new CredentialTestHelper()

	void 'Test the Session/Cookie name and ValidationExpression validation of Credential'() {
		expect:
			createTestCredential(method, sessionName, valExp, authUrl, renewUrl, httpMethod, requestMode).validate() == result

		where:
		method					| sessionName	| valExp				 | authUrl		 | renewUrl		| httpMethod 	| requestMode		| result
			am.BASIC_AUTH		| 'a@cookie:b'	| 'status code equal 200'| 'http://b.ic' | '' 			| null			| null				| true		// HAPPY PATH
			am.BASIC_AUTH		| 'bogus'		| 'bogus'				 | 'http://b.ic' | '' 			| null			| null				| false		// Bogus validation expression will fail
			am.BASIC_AUTH		| ''			| '' 					 | ''			 | '' 			| null			| null				| false		// Session name is mandatory for BASIC_AUTH
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
	 * All these tests should fail because of the OfSameProjectConstraint. These are all the happy path tests, from the previous test, but
	 * using a bad credential, where credential.project.id != credential.provider.project.id .
	 *
	 * The previous test running without breaking for the happy path, is the happy path for OfSameProjectConstraint working.
	 */
	void 'Test validation of Credential with the project being of a different provider'() {
		expect:
			createBadTestCredential(method, sessionName, valExp, authUrl, renewUrl, httpMethod, requestMode).validate() == result
		where:
			method        | sessionName | valExp  | authUrl       | renewUrl      | httpMethod | requestMode | result
			am.BASIC_AUTH | ''          | ''      | ''            | ''            | null       | null        | false
			am.BASIC_AUTH | 'bogus'     | 'bogus' | ''            | ''            | null       | null        | false
			am.JWT        | ''          | ''      | 'http://b.ic' | 'http://p.ic' | null       | null        | false
			am.JWT        | 'bogus'     | 'bogus' | 'http://b.ic' | 'http://p.ic' | null       | null        | false
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
		return credentialTestHelper.createCredential(project, provider, authMethod,
				sessionName, validationExp, authUrl, renewUrl, httpMethod, requestMode)
	}

	/**
	 * Create a Credential domain object with the given parameters that has a project, that is different from
	 * the one the provider has.
	 *
	 * @param authMethod
	 * @param sessionName
	 * @param validationExp
	 * @param authUrl
	 * @param renewUrl
	 * @param httpMethod
	 * @param requestMode
	 *
	 * @return The bad Credential domain object
	 */
	private Credential createBadTestCredential(authMethod, sessionName, validationExp, authUrl, renewUrl, httpMethod, requestMode) {
		Project project = projectTestHelper.createProject()
		Project otherProject = projectTestHelper.createProject()
		Provider provider = providerTestHelper.createProvider(project)
		return credentialTestHelper.createCredential(
			otherProject,
			provider,
			authMethod,
			sessionName,
			validationExp,
			authUrl,
			renewUrl,
			httpMethod,
			requestMode
		)
	}
}