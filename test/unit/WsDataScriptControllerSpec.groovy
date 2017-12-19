import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(WsDataScriptController)
@TestMixin(DomainClassUnitTestMixin)
class WsDataScriptControllerSpec extends Specification {

    void setupSpec () {
        mockDomain(DataScript)
    }

    def setup () {
    }

    def cleanup () {
    }

    void 'test can response with errors for an empty body json content while testing a script' () {

        given:
            request.contentType = JSON_CONTENT_TYPE
            request.json = ''

        when: 'Controller test an script content'
            controller.testScript()

        then: 'Controller response contains a status error message and an array with errors'
            with(response.json) {
                errors == ['Invalid parameters']
                status == 'error'
            }
    }

    void 'test can response with errors for a json request without filename parameter while testing a script' () {

        given:
            request.contentType = JSON_CONTENT_TYPE
            request.json = '{ script: "console on" }'

        when: 'Controller test an script content'
            controller.testScript()

        then: 'Controller response contains a status error message and an array with errors'
            with(response.json) {
                errors == ['Invalid parameters']
                status == 'error'
            }
    }

    void 'test can response with errors for an empty body json content while checking script syntax' () {

        given:
            request.contentType = JSON_CONTENT_TYPE
            request.json = ''

        when: 'Controller test an script content'
            controller.checkSyntax()

        then: 'Controller response contains a status error message and an array with errors'
            with(response.json) {
                errors == ['Invalid parameters']
                status == 'error'
            }
    }

    void 'test can response with errors for a json request without filename parameter while checking script syntax' () {

        given:
            request.contentType = JSON_CONTENT_TYPE
            request.json = '{ script: "console on" }'

        when: 'Controller test an script content'
            controller.checkSyntax()

        then: 'Controller response contains a status error message and an array with errors'
            with(response.json) {
                errors == ['Invalid parameters']
                status == 'error'
            }
    }

    void 'test can response with a console output for a json request with correct parameters' () {

        given:
            request.contentType = JSON_CONTENT_TYPE
            request.json = '{ script: "console on" , fileName: "dataSet.csv"}'

        and:
            Project project = Mock(Project)
            SecurityService securityService = Mock(SecurityService) {
                getUserCurrentProject() >> project
            }

        and:
            ScriptProcessorService scriptProcessorService = Mock(ScriptProcessorService) {
                testScript(_, _, _) >> [
                        consoleLog: 'Console status changed: on',
                        data      : [:]
                ]
            }

        when: 'Controller test an script content'
            controller.securityService = securityService
            controller.scriptProcessorService = scriptProcessorService
            controller.testScript()

        then: 'Controller response contains console out'
            with(response.json) {
                status == 'success'
                with(data) {
                    consoleLog == 'Console status changed: on'
                    data == [:]
                }
            }
    }


    void 'test can response with a console output for a json request with correct parameters while saving a script' () {

        given:
            request.contentType = JSON_CONTENT_TYPE
            request.json = '{ id: 1, script: "console on" , fileName: "dataSet.csv"}'

        and:
            Project project = Mock(Project)
            SecurityService securityService = Mock(SecurityService) {
                getUserCurrentProject() >> project
            }

        and:
            ScriptProcessorService scriptProcessorService = Mock(ScriptProcessorService) {
                saveScript(_, _) >> { dataScript, script -> dataScript }
            }

        and:
            DataScript dataScript = GroovyMock(DataScript) {
                getProject() >> project
                getId() >> 1l
            }

            GroovyMock(DataScript, global: true)
            DataScript.get(_) >> { dataScript }

        when: 'Controller test an script content'
            controller.securityService = securityService
            controller.scriptProcessorService = scriptProcessorService
            controller.saveScript()

        then: 'Controller response contains status success'
            with(response.json) {
                status == 'success'
            }
    }

}