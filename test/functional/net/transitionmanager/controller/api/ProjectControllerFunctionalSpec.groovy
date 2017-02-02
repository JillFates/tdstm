package net.transitionmanager.controller.api

import grails.plugins.rest.client.RestBuilder
import grails.test.spock.IntegrationSpec
import groovy.json.JsonOutput
import net.transitionmanager.domain.UserLogin
import spock.lang.Shared

import javax.servlet.http.HttpServletResponse

class ProjectControllerFunctionalSpec extends IntegrationSpec implements LoginAs {

    final static String TEST_USERNAME = 'watson'
    final static String TEST_PASSWORD = 'Foobar123!'

    //TODO: Grails 3 Migration
    // Remote this when migration to Grails 3
    // In Grails 3, sever port is random and gets injected automatically
    final static String serverPort = '8080'

    @Shared
    def grailsApplication

    String appName() {
        //TODO: Grails 3 Migration
        // If Grails 3 return ''

        // for Grails 2
        return "/${grailsApplication.metadata.'app.name'}"
    }

    @Override
    String grailServerUrl() {
        "http://localhost:${serverPort}${appName()}"
    }

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

    def "Validate unsucessful authentication"() {
        when:
        String jsonString = JsonOutput.toJson([username: 'sherlock', password: 'secret'])
        RestBuilder rest = new RestBuilder()
        def resp = rest.post("${grailServerUrl()}/api/login") {
            header("Accept", "application/json")
            header("Content-Type", "application/json")
            json jsonString
        }

        then:
        resp.status == HttpServletResponse.SC_UNAUTHORIZED
    }

    def "PUT /api/projects endpoint is secured"() {
        when: 'Unauthenticated requests to update a project for version 1.0'
        RestBuilder rest = new RestBuilder()
        def urlString = "${grailServerUrl()}/api/projects/1" as String
        println urlString
        def resp = rest.put(urlString) {
            header("Accept-Version", "1.0")
        }

        then: 'return unauthorized'
        resp.status == HttpServletResponse.SC_UNAUTHORIZED
    }

    def "test /api/projects/ endpoint is secured"() {
        when: 'Requesting projects for version 1.0'
        RestBuilder rest = new RestBuilder()
        def resp = rest.get("${grailServerUrl()}/api/projects/") {
            header("Accept-Version", "1.0")
        }

        then: 'returns unauthorized, endpoit is secured'
        resp.status == HttpServletResponse.SC_UNAUTHORIZED
    }

    def "test description is present in /api/projects json payload of Api 1.0"() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def accessToken = loginAs(TEST_USERNAME, TEST_PASSWORD).accessToken

        then:
        accessToken

        when: 'Requesting projects for version 1.0'
        def resp = rest.get("${grailServerUrl()}/api/projects") {
            accept("application/json")
            header("Accept-Version", "1.0")
            header("Authorization", "Bearer ${accessToken}")
        }

        then:
        resp.status == HttpServletResponse.SC_OK

        and: 'the response is a JSON Payload'
        resp.headers.get('Content-Type') == ['application/json;charset=UTF-8']

        and: 'json payload contains an array of projects with a description property'
        resp.json.each {
            assert it.keySet().contains('description')
        }
    }

    def "test description is NOT present in projects json payload of Api 2.0"() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def accessToken = loginAs(TEST_USERNAME, TEST_PASSWORD).accessToken

        then:
        accessToken

        when: 'Requesting projects for version 2.0'
        def resp = rest.get("${grailServerUrl()}/api/projects") {
            accept("application/json")
            header("Accept-Version", "2.0")
            header("Authorization", "Bearer ${accessToken}")
        }

        then: 'the request was successful'
        resp.status == HttpServletResponse.SC_OK

        and: 'the response is a JSON Payload'
        resp.headers.get('Content-Type') == ['application/json;charset=UTF-8']

        and: 'json payload contains an array of projects, those projects do not contain a description property'
        resp.json.each {
            assert !it.keySet().contains('description')
        }
    }

    def "Validate failed API call with JWT token to endpoint that account does not have permission to execute."() {
        given:
        RestBuilder rest = new RestBuilder()

        when:
        def accessToken = loginAs(TEST_USERNAME, TEST_PASSWORD).accessToken

        then:
        accessToken

        when: 'Request projects list to get a project id'
        def resp = rest.get("${grailServerUrl()}/api/projects") {
            accept("application/json")
            header("Accept-Version", "1.0")
            header("Authorization", "Bearer ${accessToken}")
        }
        def projectId = resp.json.first()['id'] as Long

        then:
        resp.status == HttpServletResponse.SC_OK

        when:
        resp = rest.get("${grailServerUrl()}/api/projects/${projectId}") {
            accept("application/json")
            header("Accept-Version", "1.0")
            header("Authorization", "Bearer ${accessToken}")
        }

        then:
        resp.status == HttpServletResponse.SC_FORBIDDEN
    }

    def "Validate successful API call with JWT token to an endpoint for which the logged account does have permission to execute."() {

        given:
        RestBuilder rest = new RestBuilder()

        when:
        def accessToken = loginAs(TEST_USERNAME, TEST_PASSWORD).accessToken

        then:
        accessToken

        when: 'Request projects list to get a project id'
        def resp = rest.get("${grailServerUrl()}/api/projects") {
            accept("application/json")
            header("Accept-Version", "1.0")
            header("Authorization", "Bearer ${accessToken}")
        }
        def projectId = resp.json.first()['id'] as Long

        then: 'the request was successful'
        resp.status == HttpServletResponse.SC_OK

        when:
        resp = rest.get("${grailServerUrl()}/api/projects/${projectId}") {
            accept("application/json")
            header("Accept-Version", "2.0")
            header("Authorization", "Bearer ${accessToken}")
        }

        then:
        resp.status == HttpServletResponse.SC_OK
    }
}
