import com.tdsops.tm.enums.domain.SecurityRole
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.LicenseAdminService
import net.transitionmanager.service.SecurityService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.junit.Ignore
import spock.lang.Specification
import spock.lang.Narrative
import spock.lang.Subject
import spock.lang.See
import net.transitionmanager.domain.License
import net.transitionmanager.domain.Project
import grails.test.mixin.TestFor

/**
 * Created by estebancantu on 02/02/17.
 */

@Narrative('''
This unit test class is intended to test the License Admin Process in the following scenarios:

- Request License
- Resubmit Request
- Manually Submit Request
- Apply License
- Delete License

''')

@See('https://support.transitionmanager.com/browse/TM-5965')

@TestFor(LicenseAdminService)
class LicenseAdminServiceIntegrationTests extends Specification {


    SecurityService securityService
    GrailsApplication grailsApplication

    private String testEmail = 'sample@sampleEmail.com'
    private String testRequestNote = 'Test request note'

    private Project project
    private Person adminPerson
    UserLogin adminUser
    private ProjectTestHelper projectTestHelper = new ProjectTestHelper()
    private PersonTestHelper personTestHelper = new PersonTestHelper()



    void setup() {

        // Enable the License Admin to be able to issue license requests
        grailsApplication.config.tdstm?.license?.enabled = true
        service.initialize()

        // Create and admin user to be able to login
        project = projectTestHelper.createProject()
        project = projectTestHelper.createProject()

        adminPerson = personTestHelper.createStaff(project.owner)
        assert adminPerson
        adminUser = personTestHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])

    }

    def '01. Test the AdminPerson and AdminUser are setup correctly, and that the License Admin module is enabled'() {

        expect: 'admin person and user are setup correctly'
            adminPerson
            adminPerson.userLogin
            adminUser
            adminUser.active == 'Y'
        and: 'the License Admin module is enabled'
            service.isAdminEnabled()
    }

    def '02. A user with admin level privileges runs TransitionManager and generates a License Request'() {

        setup: 'log in with an admin person so that'
            License licenseRequest
            securityService.assumeUserIdentity(adminUser.username, false)
            println "Performed securityService.assumeUserIdentity(adminUser.username) with ${adminUser.username}"
        expect: 'the admin user is logged and'
            securityService.isLoggedIn()
            securityService.hasPermission(adminUser, Permission.AdminUtilitiesAccess)

        when: "a new License Request is generated"
            licenseRequest = service.generateRequest(null, project.owner, testEmail, License.Environment.DEMO.toString(), project.id, testRequestNote)
        then: "the License Request is created correctly"
            licenseRequest
            licenseRequest.email == testEmail
            licenseRequest.requestNote == testRequestNote
            licenseRequest.owner == project.owner
            licenseRequest.environment == License.Environment.DEMO //as License.Environment
            licenseRequest.status == License.Status.PENDING
            licenseRequest.method == License.Method.MAX_SERVERS
            // type
            // project
            // requestDate
            // activationDate
        and: "the License Request is correctly persisted and can be retrieved"
            License savedLicenseRequest = License.findById(licenseRequest.id)
            savedLicenseRequest
            savedLicenseRequest.email == licenseRequest.email
            savedLicenseRequest.requestNote ==  licenseRequest.requestNote
            savedLicenseRequest.owner ==  licenseRequest.owner
            savedLicenseRequest.environment ==  licenseRequest.environment
            licenseRequest.status == License.Status.PENDING
            licenseRequest.method == License.Method.MAX_SERVERS
            // type
            // project
            // requestDate
            // activationDate

        when: "a new License Request with no projectId attached to it"
            def noProjectId = null
            licenseRequest = service.generateRequest(null, project.owner, testEmail, License.Environment.DEMO.toString(), noProjectId, testRequestNote)
        then: "When no projectId is used, the License should have errors and an exception should be thrown"
            licenseRequest.hasErrors()
            thrown(DomainUpdateException)

        when: "an attempt to retrieve a License Request that does not exist is made"
            License nonexistentLicense = License.findById(-1)
        then: "there should be no License Request present"
            nonexistentLicense == null
    }

    // TODO test for non-admin user

    @spock.lang.Ignore
    def "04. Delete a License Request"() {

        setup: 'log in with an admin person and create a new License Request so that'
            License licenseRequest
            securityService.assumeUserIdentity(adminUser.username, false)
            println "Performed securityService.assumeUserIdentity(adminUser.username) with ${adminUser.username}"
            licenseRequest = service.generateRequest(null, project.owner, testEmail, License.Environment.DEMO.toString(), project.id, testRequestNote)
        expect: 'the admin user is logged in and the License Request is created'
            securityService.isLoggedIn()
            securityService.hasPermission(adminUser, Permission.AdminUtilitiesAccess)
            licenseRequest

        when: "the License Request is deleted"
            boolean result = service.deleteLicense(licenseRequest.id)
        then: "the license no longer exist on the server"
            result
            License deletedLicense = License.get(licenseRequest.id)
            deletedLicense == null

        when: "a License Request with a non-existent id is trying to be deleted"
            def nonexistentId = -1
            result = service.deleteLicense(nonexistentId)
        then: "the method should return false."
            result == false
    }

    @Ignore
    void "resubmit request" () {
        // No implemented yet on the service
    }

    // TODO check to retreive non-existent licenses and check for nullity
    @Ignore
    void "manually submit request" () {
        // TODO check hash returned. Here we can check how the hash is formed
        // Generates a license hash with some parameters. Then, we have to retrieve those parameters,
        // desencrypt them and check that are the same used to generate the license.
        def encodedLicense = lic.toEncodedMessage()

        // String body = new String(Base64.encodeBase64(Smaz.compress(toJsonString())))
    }


    // APPLY LICENSE: USE LICENSE MANAGER TEST HELPER
    // TODO then for other tests, the license manager (LicenseManagerService) should be enabled to check on the license requests.
    // or use a License Manager Test Helper

}