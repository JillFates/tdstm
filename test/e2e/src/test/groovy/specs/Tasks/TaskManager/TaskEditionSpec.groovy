package specs.Tasks.TaskManager

import geb.spock.GebReportingSpec
import pages.Tasks.TaskManager.TaskCreationPage
import pages.Tasks.TaskManager.TaskDetailsPage
import pages.Tasks.TaskManager.TaskEditionPage
import pages.Tasks.TaskManager.TaskManagerPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class TaskEditionSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Edit
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static taskName = baseName +" "+ randStr + " Task For E2E Created"
    static taskNameEdit = baseName +" "+ randStr + " Task For E2E Edited"
    static taskOldStatus = "Pending"
    static taskStatus = "Ready"
    static taskEvent = "Buildout"
    static taskPerson = "Unassigned" //TODO verify other staff id values
    static taskTeam = "Unassigned"  //TODO verify other team id values
    static taskAssetType = "Applications"
    static taskAssetName
    static taskInsLink = "https://www.transitionaldata.com"
    static taskNote = "This is a Note for "+ baseName +" "+ randStr + " Task For E2E Edited"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()

        // TODO Note by CN: :'( We'd need to call the ApplicationCreationPage and send the Name we'd need to Edit via a Parameter!
        // TODO NOT to perform the Creation Option in the setupSpec() Method > A New Ticket will be handle separately
        at MenuPage
        tasksModule.goToTasksManager()
        at TaskManagerPage
        waitFor {clickCreateTask()}
        at TaskCreationPage
        waitFor {tcModalLoading.hasClass("ng-hide")}
        tcModalTaskName = taskName
        tcModalEventSelector = taskEvent
        tcModalStatusSelector = taskOldStatus
        waitFor { tcModalSaveBtn.click() }
        commonsModule.waitForTaskModal()
        at TaskManagerPage
        // set up starts making sure no event is in context
        selectEvent("All")
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Opening the Edit Task Option"() {
        given: 'The User is on the Task Manager Section'
            at TaskManagerPage
        when: 'The User searches by a specific Task'
            waitFor {tmDescriptionTColFlt.click()}
            tmDescriptionTColFlt = taskName
            waitFor{tmFirstElementDesc.text() == taskName}
        and: 'The User Clicks the "Edit" Button'
            waitFor{tmFirstElementTaskTbl.click()}

        then: 'The Tasks Edition section should be Displayed'
            at TaskEditionPage
    }

    def "2. Edit Task - Changing Fields and static Dropdowns"() {
        given: 'The User is on the Task Edition Section'
            at TaskEditionPage
        when: 'The User Modifies some static values such as Name, Person, Team, Event, Link, Status, Note'
            waitFor {teModalLoading.hasClass("ng-hide")}
            waitFor {teModalTaskName == taskName}
            teModalTaskName = taskNameEdit
            teModalPersonSelector = taskPerson
            teModalTeamSelector = taskTeam
            teModalEventSelector = taskEvent
            teModalInstructionsLink = taskInsLink
            teModalStatusSelector = taskStatus
            teModalNote = taskNote

        then: 'The User should remain in the Taks Edition Section'
            at TaskEditionPage
    }

    def "3. Edit Task - Adding Asset Type and Name"() {
        given: 'The User is on the Task Edition Page'
            at TaskEditionPage
        when: 'The User modifies some values such as Asset Type, Asset Name'
            teModalAssetTypeSelector = taskAssetType
            waitFor { teModalAssetNameSelector.find("span", text: "Please select") }
            teModalAssetNameSelector.click()
            waitFor { teModalAssetNameSelValues.size() > 1 }
            def assetName = CommonActions.getRandomOption(teModalAssetNameSelValues.find("div", class: "select2-result-label"))
            taskAssetName = assetName.text()
            assetName.click()
        then: 'The User should remain in the Taks Edition Section'
            at TaskEditionPage
    }

    def "4. Edit Task - Adding Predeccessor and Successor"() {
        given: 'The User is on the Task Edition Page'
            at TaskEditionPage
        when: 'The User adds some values such as Predecessor and Successor'
            teModalAddPredecessorBtn.click()
            waitFor {teModalPredecessorDD.click()}
            waitFor {teModalPredecessorUl}
            def predecessor = CommonActions.getSelectRandomOption(teModalPredecessorOptions)
            waitFor {predecessor.click()}
            teModalAddSuccessorBtn.click()
            waitFor {teModalSuccessorDD.click()}
            waitFor {teModalSuccessorUl}
            def sucessor = CommonActions.getSelectRandomOption(teModalSuccessorOptions)
            waitFor {sucessor.click()}
        then: 'The User should remain in the Taks Edition Section'
            at TaskEditionPage
    }

    def "5. Saving the Edited Task"() {
        given: 'The User is on the Task Edition Page'
            at TaskEditionPage
        when: 'The User clicks the "Save" Button'
            waitFor { teModalSaveBtn.click() }

        then: 'The User should be redirected to the Task Details Section'
            at TaskDetailsPage
    }

    def "6. Validating new Task Options"() {
        when: 'The User is on the Task Details Page'
            at TaskDetailsPage

        then: 'Proper values should be shown'
            waitFor{tdModalTaskName.text().trim() == taskNameEdit}
            tdModalTaskPerson.text().trim() == taskPerson
            tdModalTaskTeam.text().trim() == taskTeam
            tdModalTaskEvent.text().trim() == taskEvent
            tdModalTaskAssetName.text().trim().contains(taskAssetName)
            tdModalInstructionsLink.text().trim() == taskInsLink
            tdModalTaskStatus.text().trim() == taskStatus
            tdModalNoteLast.text().trim() == taskNote
            waitFor { tdModalCloseBtn.click() }
            at TaskManagerPage
    }

    def "7. Assigning the Task to the Current User"() {
        given: 'The User is on the Task Manager Section'
            at TaskManagerPage
        when: 'The User searches by his/her own Name'
            waitFor {tmDescriptionTColFlt.click()}
            tmDescriptionTColFlt = taskNameEdit
            waitFor{tmFirstElementDesc.text() == taskNameEdit}
            waitFor{tmFirstElementDesc.click()}
        and: 'Performs the Action'
            waitFor{tmTaskAssignMeBtn.click()}

        then: 'The User should be redirected to the Task Manager Section'
            at TaskManagerPage
    }

    def "8. Changing the Task Status to Done"() {
        given: 'The User is on the Task Manager Section'
            at TaskManagerPage
        when: 'The user clicks the "Done" Status'
            waitFor{tmTaskDoneBtn.click()}

        then: 'The the Task Status should be shown as Completed'
            at TaskManagerPage
            waitFor{tmFirstElementStatus == "Completed"}
    }

    def "9. Deleting the  Task"() {
        given: 'The User is on the Task Manager Section'
            at TaskManagerPage
        when: 'The User clicks the Option to Display the Task'
            waitFor{tmTaskDetailBtn.click()}
            at TaskDetailsPage
        and: 'The User clicks the "Delete" Button'
            withConfirm(true){waitFor {tdModalDeleteBtn.click() }}

        then: 'The User should be redirected to the Task Manager Section'
            at TaskManagerPage
    }
}