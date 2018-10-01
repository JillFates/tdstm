package specs.LandingPages

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Tasks.*
import pages.Tasks.Cookbook.*
import pages.Tasks.TaskManager.*
import spock.lang.Stepwise

/**
 * This class sweeps over the Tasks Menu.
 * It checks every single landing page.
 * @author alvaro
 */

@Stepwise
class TasksLandingPageSpec extends GebReportingSpec {
    def testKey
    static testCount

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

    def "1. The Tasks Menu has the correct number of elements"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The User Clicks on the Tasks Menu'
            tasksModule.goToTasksMenu()

        then: 'The valid clickable pages are displayed'
            tasksModule.tasksPages.size() == 7
            at MenuPage
    }

    def "2. Verify the My Tasks Landing Page"() {
        when: 'The user goes to the My Tasks page'
            tasksModule.goToMyTasks()

        then: 'The My Tasks Page is loaded successfully'
            at MyTasksPage
    }

    def "3. Verify the Task Manager Landing Page"() {
        when: 'The user goes to the Task Manager page'
            tasksModule.goToTasksManager()

        then: 'The Task Manager Page is loaded successfully'
            at TaskManagerPage
    }

    def "4. Verify the Task Graph Landing Page"() {
        when: 'The user goes to the Task Graph Page'
            tasksModule.goToTaskGraph()

        then: 'The Task Graph Page is loaded successfully'
            at TaskGraphPage
    }

    def "5. Verify the Task Timeline Landing Page"() {
        when: 'The user goes to the Task Timeline page'
            tasksModule.goToTaskTimeline()

        then: 'The  Task Timeline Page is loaded successfully'
            at TaskTimelinePage
    }

    def "6. Verify the Task Cookbook Landing Page"() {
        when: 'The user goes to the Task Cookbook page'
            tasksModule.goToTasksCookbook()

        then: 'The Task Cookbook Page is loaded successfully'
            at CookbookPage
    }

    def "7. Verify the Generation History Landing Page"() {
        when: 'The user goes to the Generation History page'
            tasksModule.goToGenerationHistory()

        then: 'The Generation History Page is loaded successfully'
            at GenerationHistoryPage
    }

    def "8. Verify the Import Tasks Landing Page"() {
        when: 'The user goes to the Import Tasks page'
            tasksModule.goToImportTasks()

        then: 'The Import Tasks Page is loaded successfully'
            at ImportTasksPage
    }

}
