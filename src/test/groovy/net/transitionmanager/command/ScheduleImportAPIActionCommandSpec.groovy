package net.transitionmanager.command


import grails.testing.web.GrailsWebUnitTest
import net.transitionmanager.project.Project
import spock.lang.Specification
import spock.lang.Unroll

class ScheduleImportAPIActionCommandSpec extends Specification implements GrailsWebUnitTest {

    void 'test sendNotification field constraints'() {
        setup:
            ScheduleImportAPIActionCommand command = new ScheduleImportAPIActionCommand()

        when:
            command.sendNotification = null

        then:
            !command.validate(['sendNotification'])
            command.errors['sendNotification'].code == 'nullable'

        when:
            command.sendNotification = true

        then:
            command.validate(['sendNotification'])
            command.errors.getErrorCount() == 0
    }

    void 'test projectId field constraints'() {
        setup:
            ScheduleImportAPIActionCommand command = new ScheduleImportAPIActionCommand()

        when:
            command.project = null

        then:
            !command.validate(['project'])
            command.errors.getErrorCount() == 1
            command.errors['project'].code == 'nullable'

        when:
            command.project = new Project()

        then:
            command.validate(['projectId'])
            command.errors.getErrorCount() == 0
    }

    @Unroll('ScheduleImportAPIActionCommand.validate() with dataScriptId: #dataScriptId and dataScriptName: #dataScriptName should have returned #expected with errorCode: #expectedErrorCode')
    void 'test dataScriptId and dataScriptName fields constraints'() {
        setup:
            ScheduleImportAPIActionCommand command = new ScheduleImportAPIActionCommand()

        when:
            command.dataScriptId = dataScriptId
            command.dataScriptName = dataScriptName

        then:
            expected == command.validate(['dataScriptId', 'dataScriptName'])
            command.errors['dataScriptName']?.code == expectedErrorCode
            command.errors.getErrorCount() == expectedErrorCount
        where:
            dataScriptId | dataScriptName | expected | expectedErrorCount | expectedErrorCode
            1233l        | 'TM-16291'     | false    | 1                  | 'api.import.must.be.one'
            null         | null           | false    | 1                  | 'api.import.must.be.one'
            null         | 'TM-16291'     | true     | 0                  | null
            1233l        | null           | true     | 0                  | null
    }
}
