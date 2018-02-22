package specs.TaskManager
import geb.spock.GebReportingSpec
import pages.TaskManager.TaskManagerPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class TaskManagerSpec extends GebReportingSpec{

    def testKey
    static testCount
    //Define the names of the tasks you will Create and Edit

    def setupSpec() {
        testCount = 0
        def username = "e2e_test_user"
        def password = "e2e_password"
        to LoginPage
        loginModule.login(username,password)
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "Go To Task Manager"() {
        testKey = "TM-XXXX"
        given:
        at MenuPage
        when:
        menuModule.goToTasksManager()
        then:
        at TaskManagerPage
    }

}

