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
class TaskEditionSpec extends GebReportingSpec{

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "QAE2E"
    static taskName = baseName +" "+ randStr + " Task For E2E Created"
    static taskNameEdit = baseName +" "+ randStr + " Task For E2E Edited"
    static taskOldStatus = "Pending"
    static taskStatus = "Ready"
    static taskEvent = "Buildout"
    static taskPerson = "Unassigned" //TODO verify other staff id values
    static taskTeam = "Unassigned"  //TODO verify other team id values
    static taskAssetType = "Applications"
    static taskAssetName = "App 1"
    static taskPredecessor = "11747: ZZ Test Task 2"
    static taskSuccessor = "11749: ZZ Test Task 3"

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
        tcModalStatusSelector = taskOldStatus
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

    def "Open Edit Task"() {
        testKey = "TM-XXXX"
        given:
        at TaskManagerPage
        when:
        waitFor {tmDescriptionTColFlt.click()}
        tmDescriptionTColFlt = taskName
        waitFor{tmFirstElementDesc.text() == taskName}
        waitFor{tmFirstElementTaskTbl.click()}
        then:
        at TaskEditionPage
    }

    def "Edit Task - Change Name, Assigned to and Team"() {
        testKey = "TM-XXXX"
        given:
        at TaskEditionPage
        when:
        waitFor {teModalLoading.hasClass("ng-hide")}
        waitFor {teModalTaskName == taskName}
        teModalTaskName = taskNameEdit
        teModalPersonSelector = taskPerson
        teModalTeamSelector = taskTeam
        teModalEventSelector = taskEvent
        teModalStatusSelector = taskStatus
        then:
        at TaskEditionPage

    }

    def "Edit Task - Add Asset Type and Name"() {
        testKey = "TM-XXXX"
        given:
        at TaskEditionPage
        when:
        teModalAssetTypeSelector = taskAssetType
        waitFor { teModalAssetNameSelector.find("span", text: "Please select") }
        teModalAssetNameSelector.click()
        waitFor { teModalAssetNameSelValues.size() > 1 }
        waitFor { teModalAssetNameSelValues.find("div", class: "select2-result-label", text: taskAssetName).click() }
        then:
        at TaskEditionPage
    }

    def "Edit Task - Add Predeccessor and Successor"() {
        testKey = "TM-XXXX"
        given:
        at TaskEditionPage
        when:
        teModalAddPredecessorBtn.click()
        waitFor {teModalPredecessorDD.click()}
        waitFor {$("li",text:taskPredecessor).click()}
        teModalAddSuccessorBtn.click()
        waitFor {teModalSuccessorDD.click()}
        waitFor {$("li",text:taskSuccessor).click()}
        then:
        at TaskEditionPage
    }

    def "Save Task"() {
        testKey = "TM-XXXX"
        given:
        at TaskEditionPage
        when:
        waitFor { teModalSaveBtn.click() }
        then:
        at TaskDetailsPage
    }

    def "Validate Task Details"() {
        testKey = "TM-XXXX"
        when:
        at TaskDetailsPage
        then:
        waitFor{tdModalTaskName.text().trim() == taskNameEdit}
        tdModalTaskPerson.text().trim() == taskPerson
        tdModalTaskTeam.text().trim() == taskTeam
        tdModalTaskEvent.text().trim() == taskEvent
        tdModalTaskAssetName.text().trim() == taskAssetName
        tdModalTaskStatus.text().trim() == taskStatus
        waitFor { tdModalCloseBtn.click() }
        at TaskManagerPage
    }

    def "Assign task to me"() {
        testKey = "TM-XXXX"
        given:
        at TaskManagerPage
        when:
        waitFor {tmDescriptionTColFlt.click()}
        tmDescriptionTColFlt = taskNameEdit
        waitFor{tmFirstElementDesc.text() == taskNameEdit}
        waitFor{tmFirstElementDesc.click()}
        waitFor{tmTaskAssignMeBtn.click()}
        then:
        at TaskManagerPage
    }

    def "Change to Done"() {
        testKey = "TM-XXXX"
        given:
        at TaskManagerPage
        when:
        waitFor{tmTaskDoneBtn.click()}
        then:
        at TaskManagerPage
        waitFor{tmFirstElementStatus == "Completed"}
    }

    def "Delete Task"() {
        testKey = "TM-XXXX"
        given:
        at TaskManagerPage
        when:
        waitFor{tmTaskDetailBtn.click()}
        at TaskDetailsPage
        withConfirm(true){waitFor {tdModalDeleteBtn.click() }}
        then:
        at TaskManagerPage
    }
}

