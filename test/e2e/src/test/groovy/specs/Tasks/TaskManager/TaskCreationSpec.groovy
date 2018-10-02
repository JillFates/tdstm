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
class TaskCreationSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Edit
    static randStr = CommonActions.getRandomString()
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

    def "1. Displaying the Task Manager Section"() {
        given: 'The User is at the Menu Page'
            at MenuPage
        when: 'User Goes to the Tasks > Task Manager Section'
            tasksModule.goToTasksManager()

        then: 'Task Manager Section should be Displayed'
            at TaskManagerPage
    }

    def "2. Opening the Create Task Pop-Up"() {
        given: 'The User is on the Task Manager Section'
            at TaskManagerPage
        when: 'The User clicks the "Create" Button'
            tmCreateTaskBtn.click()

        then: 'Task Creation Pop-Up should be displayed'
            at TaskCreationPage
    }

    def "3. Creating a brand new Task"() {
        given: 'The User is on the Task Creation Pop-Up'
            at TaskCreationPage
        when: 'The User completes all related information such as Name, Event, Status, Person, Team'
            waitFor {tcModalLoading.hasClass("ng-hide")}
            tcModalTaskName = taskName
            tcModalEventSelector = taskEvent
            tcModalStatusSelector = taskStatus
            tcModalPersonSelector = taskPerson
            tcModalTeamSelector = taskTeam
        and: 'The User clicks the "Save" Button'
            waitFor { tcModalSaveBtn.click() }
            commonsModule.waitForTaskModal()
        then: 'The User should be redirected to the Task Manager Section'
            at TaskManagerPage
    }

    def "5. Filtering out by the brand New Task on the List"() {
        given: 'The User is on the Task Manager Section'
            at TaskManagerPage
        when: 'The User filters by the brand new Task'
            waitFor {tmDescriptionTColFlt.click()}
            tmDescriptionTColFlt = taskName
            waitFor{tmFirstElementDesc.text() == taskName}
            waitFor{tmFirstElementDesc.click()}
        and: 'The User clicks on the Task'
            waitFor{tmTaskDetailBtn.click()}

        then: 'The User should be redirected to the Tasks Details Section'
            at TaskDetailsPage
    }

    def "6. Validate Task Details"() {
        when: 'The User is on the Task Details Section'
            at TaskDetailsPage

        then: 'Data that has been added should be properly displayed'
            waitFor{tdModalTaskName.text().trim() == taskName}
            tdModalTaskEvent.text().trim() == taskEvent
            tdModalTaskStatus.text().trim() == taskStatus
            tdModalTaskPerson.text().trim() == taskPerson
            tdModalTaskTeam.text().trim() == taskTeam
    }
}