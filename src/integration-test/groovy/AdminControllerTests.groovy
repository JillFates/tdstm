import com.tdsops.tm.enums.domain.SecurityRole
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.See
import spock.lang.Shared
import spock.lang.Specification
import test.helper.PersonTestHelper
/**
 * Unit test cases for the TimeUtil class
 * Note that in order to test with the HttpSession that this test spec is using the AdminController not for any thing in particular
 * but it allows the tests to access the session and manipulate it appropriately.
 */
@Integration
@Rollback
class AdminControllerTests extends Specification {

    @Autowired
    AdminController controller
    @Autowired
    WebApplicationContext ctx
    @Autowired
    SecurityService securityService
    @Autowired
    CoreService coreService

    @Shared
    def personHelper
    @Shared
    def projectHelper
    @Shared
    Project project
    @Shared
    def privPerson, adminPerson
    @Shared
    def unPrivPerson, unPrivUser
    @Shared
    boolean initialized = false

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest(ctx)
        if (!initialized) {
            personHelper = new PersonTestHelper()
            projectHelper = new ProjectTestHelper()
            adminPerson = personHelper.getAdminPerson()

            UserLogin adminUser = personHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ROLE_ADMIN}"])
            securityService.assumeUserIdentity(adminUser.username, false)
            assert securityService.isLoggedIn()
            initialized = true
        }
    }

    def cleanup() {
         RequestContextHolder.resetRequestAttributes()
    }

    def 'Test the AccountImportExport controller methods for permissions'() {
        setup:
        project = projectHelper.createProject(adminPerson.company)
        unPrivPerson = personHelper.createPerson(adminPerson, project.client, project, null, null, 'ROLE_USER')

        when:
        def x = 1
        // Need to mock the session to return the user appropriately
        //controller.importAccountsTemplate()
        then:
        x == 1
    }

    /**
     * The property <code>serviceRestartCommand</code> is defined in the tdstm-config.groovy file.
     * Basically is a OS command for creating an empty file that it will be checked later by a cron process,
     * and if the file exists then it will restart the app service.
     * The command is 'touch' and is an OS dependent command, as it is intended to be run on UNIX systems.
     * If you want to run this test on Windows, there is a workaround that consists of installing a touch command
     * for Windows. This can be done with http://www.binarez.com/touch_dot_exe/
     *
     * TODO: (oluna@tdsi.com) I think that this should not be tested since we are getting the command from a configuration file
     */
    @See('TM-7670')
    def 'Test the correct creation of the restart file in restartAppServiceAction()'() {
        setup: 'Check that the command that will be used internally to create the file is ok'
            String command = coreService.getAppConfigSetting(AdminController.APP_RESTART_CMD_PROPERTY)
            assert command
            def touchCmd = command.split()[0]   /* get the command name */
            assert touchCmd == 'touch'
        and: 'make sure the file does not exist before running the test.'
            def filePath = command.split()[1]   /*  get the file path */
            def file = new File(filePath)       /*  if the file exists, delete it */
            if (file.exists()) {
                file.delete()
            }
            !file.exists()
        when:'the restartAppServiceAction() method is called'
            def returned = controller.restartAppServiceAction()
        then:'the method executes successfully and the file gets created.'
            controller.response.status == 200
            file.exists()
    }
}
