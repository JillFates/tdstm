package specs.TaskManager
import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.TaskManager.TaskManagerPage
import pages.TaskManager.CreateTaskPage
import pages.common.LoginPage
import pages.Cookbook.UserDashboardPage
import spock.lang.Stepwise

@Stepwise
class TaskManagerSpec extends GebReportingSpec{

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

        //given:
        //at TaskManagerPage

        when:
        createTaskBtLb.click()

        then:
        at CreateTaskPage
        ctModalTitle.text() == "Create Task"

        //print CreateTaskPage.ctModalTitle.text() //Does not work if I instantiate CreateTaskPage

    }

    def "Create Task"() {


        when:
        ctModalNameTA = "QA Automation"
        waitFor { ctModalSaveBt.click() }

        then:
        ctModalNameTA == "QA Automation"
        at TaskManagerPage

    }


    def "Open Edit Task"() {
//Cambiarlo a escribir en el filtro y buscar mi tarea
        when:
        waitFor { taskTColLb.click() }
        Thread.sleep(950)
        taskTColLb.click()
        Thread.sleep(950)
        firstElementTaskTbl.click()

        then:
        at CreateTaskPage

    }


    def "Edit Task"() {

        when:
        ctModalNameTA = "QA Automation Edited"
        waitFor { ctModalSaveBt.click() }

        then:
        ctModalNameTA == "QA Automation Edited"
        at TaskManagerPage


    }


}

