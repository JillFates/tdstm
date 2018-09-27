package specs.Tasks.TaskManager

import geb.spock.GebReportingSpec
import pages.Tasks.TaskManager.TaskManagerPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class TaskManagerSpec extends GebReportingSpec {

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Edit

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

    def "1. Going To The Task Manager Section"() {
        given: 'The User is at the Menu Page'
            at MenuPage
        when: 'User Goes to the Tasks > Task Manager Section'
            tasksModule.goToTasksManager()

        then: 'Task Creation Pop-Up should be displayed'
            at TaskManagerPage
    }
}