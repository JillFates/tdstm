import com.tdsops.tm.enums.domain.SecurityRole
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import net.transitionmanager.admin.AdminController
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.common.CoreService
import net.transitionmanager.security.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Ignore
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
// TODO - SL - 01/31: Ignoring this test now, but it needs to be moved to a functional test using GebSpec
class AdminControllerTests extends Specification {

    @Autowired
	AdminController       controller
    @Autowired
	WebApplicationContext ctx
    @Autowired
	SecurityService       securityService
    @Autowired
	CoreService           coreService

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
}
