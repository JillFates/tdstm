package specs.Tasks.TaskManager

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Tasks.TaskManager.TaskCreationPage
import pages.Tasks.TaskManager.TaskDetailsPage
import pages.Tasks.TaskManager.TaskManagerPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class TaskDeletionSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Delete
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static taskName = baseName +" "+ randStr + " Task For E2E Automation for Delete"
    static taskEvent = "Buildout"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()

        at MenuPage
        menuModule.goToTasksManager()
        at TaskManagerPage
        tmCreateTaskBtn.click()
        at TaskCreationPage
        waitFor {tcModalLoading.hasClass("ng-hide")}
        tcModalTaskName = taskName
        tcModalEventSelector = taskEvent
        waitFor { tcModalSaveBtn.click() }
        commonsModule.waitForTaskModal()
        at TaskManagerPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Filtering out by Tasks on the List"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Task Manager Section'
            at TaskManagerPage
        when: 'The User searches by a specific Task'
            waitFor {tmDescriptionTColFlt.click()}
            tmDescriptionTColFlt = taskName
            waitFor{tmFirstElementDesc.text() == taskName}
        and: 'The User Clicks to display it'
            waitFor{tmFirstElementDesc.click()}
            waitFor{tmTaskDetailBtn.click()}

        then: 'The User should be redirected to the Task Details Section'
            at TaskDetailsPage
    }

    def "2. Opens up the Task Details Section"() {
        testKey = "TM-XXXX"
        when: 'The User is on the Task Details Section'
            at TaskDetailsPage

        then: 'The User waits for the Task to be shown'
            waitFor{tdModalTaskName.text().trim() == taskName}
    }

    def "3. Deleting the Task"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Task Details Section'
            at TaskDetailsPage
        when: 'The User clicks the "Delete" Button'
            withConfirm(true){waitFor {tdModalDeleteBtn.click() }}

        then: 'The User should be redirected to the Task Manager Section'
            at TaskManagerPage
    }
}