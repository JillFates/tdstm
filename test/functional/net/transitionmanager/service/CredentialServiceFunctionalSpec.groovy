package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AuthenticationMethod
import grails.test.spock.IntegrationSpec
import net.transitionmanager.controller.api.SignupHelper
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import spock.lang.Shared
import test.helper.CredentialTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonOutput

class CredentialServiceFunctionalSpec extends IntegrationSpec {
    final static String TEST_USERNAME = 'FooTestUser'
    final static String TEST_PASSWORD = 'Foobar123!'
    final static String HOSTNAME = 'http://localhost:8080/tdstm/api/login'

    @Shared
    CredentialService credentialService

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
            Project project = projectTestHelper.createProject()
            Provider provider = providerTestHelper.createProvider(project)
            Credential credential = credentialTestHelper.createAndSaveCredential(project, provider, TEST_USERNAME, TEST_PASSWORD, HOSTNAME, AuthenticationMethod.JWT)
            final Long id = credential.id

        when: 'authenticate using provided credentials for a JWT token'
            def authentication = credentialService.authenticate(id)

        then: 'the system must authenticate and return JWT basic token keys'
            null != authentication
            'access_token'      in authentication.keySet()
            'refresh_token'     in authentication.keySet()
            'token_type'        in authentication.keySet()
            'expires_in'        in authentication.keySet()
    }

    def '2. /ws/credential/enums call to get JSON map of Credential Enums'() {
        when: 'calling the endpoint'
            RestBuilder rest = new RestBuilder()
            String urlString = "${grailServerUrl()}/ws/credential/enums" as String
            def resp = rest.get(urlString) {
                accept("application/json")
            }

        then: 'return should be OK'
            resp.status == HttpServletResponse.SC_OK
            println resp
        
    }

}
