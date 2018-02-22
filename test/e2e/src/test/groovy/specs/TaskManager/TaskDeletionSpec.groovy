package specs.TaskManager

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.TaskManager.TaskCreationPage
import pages.TaskManager.TaskDetailsPage
import pages.TaskManager.TaskEditionPage
import pages.TaskManager.TaskManagerPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class TaskDeletionSpec extends GebReportingSpec{

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Delete
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "QAE2E"
    static taskName = baseName +" "+ randStr + " Task For E2E Automation for Delete"
    static taskEvent = "Buildout"

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

    def "Open Task Details"() {
        testKey = "TM-XXXX"
        when:
        at TaskDetailsPage
        then:
        waitFor{tdModalTaskName.text().trim() == taskName}
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

