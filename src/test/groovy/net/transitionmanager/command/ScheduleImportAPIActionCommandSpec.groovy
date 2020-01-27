package net.transitionmanager.command

import grails.testing.gorm.DataTest
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
            command.errors['sendNotification'].code == 'default.missing.required'

        when:
            command.sendNotification = true

        then:
            command.validate(['sendNotification'])
            command.errors.getErrorCount() == 0
    }

    void 'test project field constraints'() {
        setup:
            ScheduleImportAPIActionCommand command = new ScheduleImportAPIActionCommand()

        when:
            command.project = null

        then:
            !command.validate(['project'])
            command.errors.getErrorCount() == 1
            command.errors['project'].code == 'default.missing.required'

        when:
            command.project = new Project()

        then:
            command.validate(['projectId'])
            command.errors.getErrorCount() == 0
    }

    @Unroll('ScheduleImportAPIActionCommand.validate() with dataScriptId: #id, dataScriptName: #name and dataScriptProvider: #provider should have returned #expected with errorCode: #expectedErrorCode')
    void 'test dataScriptId, dataScriptName and dataScriptProvider fields constraints'() {
        setup:
            ScheduleImportAPIActionCommand command = new ScheduleImportAPIActionCommand()

        when:
            command.dataScriptId = id
            command.dataScriptName = name
            command.providerName = provider

        then:
            expected == command.validate(['dataScriptId', 'dataScriptName', 'providerName'])
            command.errors['providerName']?.code == expectedErrorCode
            command.errors.getErrorCount() == expectedErrorCount
        where:
            id    | name       | provider | expected | expectedErrorCount | expectedErrorCode
            /* Valid combinations */
            1233l | null       | null     | true     | 0                  | null
            null  | 'TM-16291' | 'VMWare' | true     | 0                  | null
            /* Invalid combinations based on dataScriptId field */
            1233l | 'TM-16291' | 'VMWare' | false    | 1                  | 'api.import.missing.datascript.reference'
            1233l | 'TM-16291' | null     | false    | 1                  | 'api.import.missing.datascript.reference'
            1233l | null       | 'VMWare' | false    | 1                  | 'api.import.missing.datascript.reference'
            /* Invalid combinations based on dataScriptName and dataScriptProvider combination */
            null  | 'TM-16291' | null     | false    | 1                  | 'default.missing.required'
            null  | null       | 'VMWare' | false    | 1                  | 'default.missing.required'
            null  | null       | null     | false    | 1                  | 'api.import.missing.datascript.reference'
    }
}
