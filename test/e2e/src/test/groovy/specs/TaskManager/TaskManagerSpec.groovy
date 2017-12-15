package specs.TaskManager
import geb.spock.GebReportingSpec
import pages.TaskManager.TaskDetailsPage
import pages.TaskManager.TaskEditionPage
import pages.TaskManager.TaskManagerPage
import pages.TaskManager.TaskCreationPage
import pages.common.LoginPage
import pages.Dashboards.UserDashboardPage
import spock.lang.Stepwise

@Stepwise
class TaskManagerSpec extends GebReportingSpec{

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Edit
    static taskName = "ZZ Task For E2E Automation"
    static taskNameEdit = "ZZ Task For E2E Automation Edited"

    def setupSpec() {
        testCount = 0
        given:
        to LoginPage
        when:
        username = System.properties['tm.creds.username'] ?: "e2e_test_user"
        password = System.properties['tm.creds.password'] ?: "e2e_password"
        println "setupSpec(): Login as user: e2e_test_user"
        submitButton.click()
        then:
        at UserDashboardPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "open Task Manager"() {
        testKey = "TM-XXXX"
        given:
        at UserDashboardPage
        when:
        waitFor { taskMenu.click() }
        taskManagerMenuItem.click()
        then:
        at TaskManagerPage
    }


    def "open Create Task Pop Up"() {
        testKey = "TM-XXXX"
        given:
        at TaskManagerPage
        when:
        createTaskBtLb.click()
        then:
        at TaskCreationPage
    }

    def "Create Task"() {
        testKey = "TM-XXXX"
        given:
        at TaskCreationPage
        when:
        ctModalNameTA = taskName
        waitFor { ctModalSaveBtn.click() }
        then:
        at TaskManagerPage
    }


    def "Open Edit Task"() {
        testKey = "TM-XXXX"
        given:
        at TaskManagerPage
        when:
        waitFor {descriptionTColFlt.click()}
        descriptionTColFlt = taskName
        waitFor{firstElementDesc.text() == taskName}
        waitFor{firstElementTaskTbl.click()}
        then:
        at TaskEditionPage
    }


    def "Edit Task"() {
        testKey = "TM-XXXX"
        given:
        at TaskEditionPage
        when:
        waitFor {etModalNameTA == taskName}
        etModalNameTA = taskNameEdit
        waitFor { etModalSaveBtn.click() }
        then:
        waitFor{etModalEdited == taskNameEdit}
        at TaskDetailsPage
    }

    def "Task Details"() {
        testKey = "TM-XXXX"
        when:
        at TaskDetailsPage
        then:
        waitFor{tdModalTaskName.text() == taskNameEdit}
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

