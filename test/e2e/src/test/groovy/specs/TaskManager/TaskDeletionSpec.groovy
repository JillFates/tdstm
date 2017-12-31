package specs.TaskManager

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.TaskManager.TaskCreationPage
import pages.TaskManager.TaskDetailsPage
import pages.TaskManager.TaskEditionPage
import pages.TaskManager.TaskManagerPage
import pages.common.LoginPage
import pages.common.MenuPage
import spock.lang.Stepwise

@Stepwise
class TaskDeletionSpec extends GebReportingSpec{

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "QAE2E"
    static taskName = baseName +" "+ randStr + " Task For E2E Automation for Delete"
    static taskNameEdit = baseName +" "+ randStr + " Task For E2E Edited"
    static taskStatus = "Started"
    static taskEvent = "Buildout"
    static taskPerson = "AUTO" //TODO verify other staff id values
    static taskTeam = "AUTO"  //TODO verify other team id values
    static taskAssetType = "Applications"
    static taskAssetName = "App 1"

    def setupSpec() {
        testCount = 0
        def username = "e2e_test_user"
        def password = "e2e_password"
        to LoginPage
        loginModule.login(username,password)
        at MenuPage
        menuModule.goToTasksManager()
        at TaskManagerPage
        tmCreateTaskBtn.click()
        at TaskCreationPage
        waitFor {tcModalLoading.hasClass("ng-hide")}
        tcModalTaskName = taskName
        tcModalEventSelector = taskEvent
        tcModalStatusSelector = taskStatus
        tcModalPersonSelector = taskPerson
        tcModalTeamSelector = taskTeam
        waitFor { tcModalSaveBtn.click() }
        at TaskManagerPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "Task Details"() {
        testKey = "TM-XXXX"
        when:
        at TaskDetailsPage
        then:
        waitFor{tdModalTaskName.text().trim() == taskNameEdit}
        waitFor{tdModalTaskEvent.text().trim() == taskEvent}
        waitFor{tdModalTaskStatus.text().trim() == taskStatus}
    }

    def "Delete Task"() {
        testKey = "TM-XXXX"
        at TaskDetailsPage
        when:
        withConfirm(true){waitFor {tdModalDeleteBtn.click() }}
        then:
        at TaskManagerPage
    }

}

