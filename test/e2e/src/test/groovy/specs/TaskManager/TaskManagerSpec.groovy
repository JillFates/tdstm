package specs.TaskManager
import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import javafx.scene.control.Alert
import pages.TaskManager.TaskManagerPage
import pages.TaskManager.CreateTaskPage
import pages.common.LoginPage
import pages.Dashboards.UserDashboardPage
import spock.lang.Stepwise
import spock.util.Exceptions

@Stepwise
class TaskManagerSpec extends GebReportingSpec{

    //Define the names of the tasks you will Create and Edit
    def  taskName = "01 Testing QA Automation Task Manager Run"
    def  taskNameEdit = "QA Automation Edited"

    def setupSpec() {
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


    def "open Task Manager"() {
        given:
        at UserDashboardPage

        when:
        waitFor { taskMenu.click() }
        taskManagerMenuItem.click()

        then:
        at TaskManagerPage

    }


    def "open Create Task Pop Up"() {

        when:
        createTaskBtLb.click()

        then:
        at CreateTaskPage
        ctModalTitle.text() == "Create Task"


    }

    def "Create Task"() {

        when:

        ctModalNameTA = taskName
        waitFor { ctModalSaveBt.click() }



        then:

        at TaskManagerPage

    }


    def "Open Edit Task"() {

        when:
        waitFor {descriptionTColFlt.click()}
        descriptionTColFlt = taskName
        waitFor{firstElementDesc.text() == taskName}
        waitFor{firstElementTaskTbl.click()}

        then:
        at CreateTaskPage
        ctModalTitle.text() == "Edit Task"

    }


    def "Edit Task"() {

        when:
        ctModalNameTA = taskNameEdit
        waitFor { ctModalSaveBt.click() }

        then:
        waitFor{ctModalEdited == taskNameEdit}
        at CreateTaskPage

    }


    def "Delete Task"() {

        when:
        withConfirm(true){waitFor { deletebtn.click() }}

        then:
        at TaskManagerPage

    }


}

