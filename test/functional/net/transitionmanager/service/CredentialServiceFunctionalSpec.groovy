package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.AuthenticationRequestMode
import grails.plugins.rest.client.RestBuilder
import grails.test.spock.IntegrationSpec
import net.transitionmanager.controller.api.SignupHelper
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.codehaus.groovy.grails.commons.GrailsApplication
import spock.lang.Shared
import test.helper.CredentialTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper

import javax.servlet.http.HttpServletResponse

class CredentialServiceFunctionalSpec extends IntegrationSpec {
    final static String TEST_USERNAME = 'FooTestUser'
    final static String TEST_PASSWORD = 'Foobar123!'
    final static String JWT_LOGIN_ENDPOINT = 'http://localhost:8080/tdstm/api/login'
    final static String FORM_LOGIN_ENDPOINT = 'http://localhost:8080/tdstm/auth/signIn'

    @Shared
    CredentialService credentialService
    GrailsApplication grailsApplication

    ProjectTestHelper projectTestHelper = new ProjectTestHelper()
    ProviderTestHelper providerTestHelper = new ProviderTestHelper()
    CredentialTestHelper credentialTestHelper = new CredentialTestHelper()

    def setupSpec() {
        def signupHelper = new SignupHelper()
        signupHelper.savePersonWithRoles(TEST_USERNAME, TEST_PASSWORD, ['ADMIN'])
        def username = TEST_USERNAME
        signupHelper.disablePasswordExpirationByUsername(username)
    }

    def cleanupSpec() {
        def signupHelper = new SignupHelper()
        signupHelper.deleteUserLoginByUsername(TEST_USERNAME)
    }

    def "1. credentialService validate successful authentication using JWT using TDSTM as token issuer"() {
        given: 'a credential'
			final String sessionName = 'access_token@json:access_token'
			final String validationExpression = 'status code equal "200"'
            Project project = projectTestHelper.createProject()
            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createAndSaveCredential(project, provider, TEST_USERNAME,
					TEST_PASSWORD, JWT_LOGIN_ENDPOINT, AuthenticationMethod.JWT,
					sessionName, AuthenticationRequestMode.FORM_VARS, validationExpression)

        when: 'authenticate using provided credentials for a JWT token'
            def authentication = credentialService.authenticate(credential)

        then: 'the system must authenticate and return JWT basic token keys'
            null 			!= authentication
            'access_token' 	== authentication.sessionName
			null 			!= authentication.sessionValue
    }

    def '2. /ws/credential/enums call to get JSON map of Credential Enums'() {
        when: 'calling the endpoint'
            RestBuilder rest = new RestBuilder()
            String urlString = "${grailsApplication.config.grails.serverURL}/ws/credential/enums" as String
            def resp = rest.get(urlString) {
                accept("application/json")
            }

        then: 'return should be OK'
            resp.status == HttpServletResponse.SC_OK
    }

	def "3. credentialService validate successful authentication using FORM VARS using TDSTM as session issuer"() {
		given: 'a credential'
			final String sessionName = 'JSESSIONID@cookie:JSESSIONID'
			final String validationExpression = 'header Location contains "/tdstm/dashboard/userPortal"'
			Project project = projectTestHelper.createProject()
			Provider provider = providerTestHelper.createProvider(project)
			Credential credential = credentialTestHelper.createAndSaveCredential(project, provider, TEST_USERNAME,
					TEST_PASSWORD, FORM_LOGIN_ENDPOINT, AuthenticationMethod.COOKIE,
					sessionName, AuthenticationRequestMode.FORM_VARS, validationExpression)

		when: 'authenticate using provided credentials for a FORM VARS login'
			def authentication = credentialService.authenticate(credential)

		then: 'the system must authenticate and return session cookie'
			null 			!= authentication
			'JSESSIONID' 	== authentication.sessionName
			null 			!= authentication.sessionValue
	}

}
