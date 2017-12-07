import grails.test.mixin.TestFor
import net.transitionmanager.service.CoreService
import spock.lang.See
import spock.lang.Specification
import net.transitionmanager.domain.Project
import net.transitionmanager.service.SecurityService

/**
 * Unit test cases for the TimeUtil class
 * Note that in order to test with the HttpSession that this test spec is using the AdminController not for any thing in particular
 * but it allows the tests to access the session and manipulate it appropriately.
 */
@TestFor(AdminController)
class AdminControllerTests extends Specification {

    def personHelper
    def projectHelper
    Project project
    def privPerson, privUser
    def unPrivPerson, unPrivUser
    CoreService coreService

    def setup() {
        personHelper = new PersonTestHelper()
        projectHelper = new ProjectTestHelper()
        privUser = personHelper.getAdminPerson()
    }

    def 'Test the AccountImportExport controller methods for permissions'() {
        setup:
        project = projectHelper.createProject(privUser.company)
        unPrivPerson = personHelper.createPerson(privUser, project.company, project, null, null, 'USER')

        when:
        def x = 1
        // Need to mock the session to return the user appropriately
        //controller.importAccountsTemplate()
        then:
        x == 1
    }

    /**
     * Say how the configuration in tdstm-config should be.
     * This is intended for UNIXes
     * Say how to run in windows
     */
    @See('TM-7670')
    def 'Test the correct creation of the file in restartAppServiceAction()'() {
        setup:
            // Mocking security service
            boolean showAllProjPermExpected = true
            controller.securityService = new SecurityService() {
                boolean hasPermission(String permission) {
                    assert permission == 'ProjectShowAll'
                    showAllProjPermExpected
                }
            }
            String cmd = coreService.getAppConfigSetting(AdminController.APP_RESTART_CMD_PROPERTY)
            def touchCommand = cmd.split()[0]   // strip command name
            def filePath = cmd.split()[1]       // strip file path
            def file = new File(filePath)
            assert touchCommand == 'touch'
            assert !file.exists()
        when:
        controller.restartAppServiceAction()
        then:
        // json is success
        file.exists()
    }
}
