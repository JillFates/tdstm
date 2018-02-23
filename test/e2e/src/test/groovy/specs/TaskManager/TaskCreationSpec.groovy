package specs.TaskManager

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.TaskManager.TaskCreationPage
import pages.TaskManager.TaskDetailsPage
import pages.TaskManager.TaskManagerPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class TaskCreationSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "QAE2E"
    static taskName = baseName +" "+ randStr + " Task For E2E Created"
    static taskStatus = "Hold"
    static taskEvent = "Buildout"
    static taskPerson = "Unassigned" //TODO verify other staff id values
    static taskTeam = "Unassigned"  //TODO verify other team id values

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "Go To Task Manager"() {
        testKey = "TM-XXXX"
        given:
        at MenuPage
        when:
        menuModule.goToTasksManager()
        then:
        at TaskManagerPage
    }

    def "Open Create Task Pop Up"() {
        testKey = "TM-XXXX"
        given:
        at TaskManagerPage
        when:
        tmCreateTaskBtn.click()
        then:
        at TaskCreationPage
    }

    def "Create Task"() {
        testKey = "TM-XXXX"
        given:
        at TaskCreationPage
        when:
        waitFor {tcModalLoading.hasClass("ng-hide")}
        tcModalTaskName = taskName
        tcModalEventSelector = taskEvent
        tcModalStatusSelector = taskStatus
        tcModalPersonSelector = taskPerson
        tcModalTeamSelector = taskTeam
        waitFor { tcModalSaveBtn.click() }
        then:
        at TaskManagerPage
    }

    def "Filter Task on List"() {
        testKey = "TM-XXXX"
        given:
        at TaskManagerPage
        when:
        waitFor {tmDescriptionTColFlt.click()}
        tmDescriptionTColFlt = taskName
        waitFor{tmFirstElementDesc.text() == taskName}
        waitFor{tmFirstElementDesc.click()}
        waitFor{tmTaskDetailBtn.click()}
        then:
        at TaskDetailsPage
    }

    def "Validate Task Details"() {
        testKey = "TM-XXXX"
        when:
        at TaskDetailsPage
        then:
        waitFor{tdModalTaskName.text().trim() == taskName}
        tdModalTaskEvent.text().trim() == taskEvent
        tdModalTaskStatus.text().trim() == taskStatus
        tdModalTaskPerson.text().trim() == taskPerson
        tdModalTaskTeam.text().trim() == taskTeam
    }
}