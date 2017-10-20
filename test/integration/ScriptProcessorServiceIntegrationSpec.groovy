package net.transitionmanager.service.dataingestion

import net.transitionmanager.domain.Project
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
class ScriptProcessorServiceIntegrationSpec extends Specification {

    ScriptProcessorService scriptProcessorService

    def setup () {
    }

    def cleanup () {
    }

    def "1) test can execute a script" () {

        given:
            Project project = new Project()
        and:
            String script = "console on"
        and:
            String fileName = ""

        when: 'Service executes the script using the file content as an input data'

            scriptProcessorService.execute(project, script, fileName)

        then:
            true == true

    }
}
