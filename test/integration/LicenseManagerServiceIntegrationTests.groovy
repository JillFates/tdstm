import com.tdsops.tm.enums.domain.SecurityRole
import net.transitionmanager.domain.License
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.LicenseAdminService
import net.transitionmanager.service.LicenseManagerService
import org.apache.commons.lang3.StringUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.joda.time.DateTime
import spock.lang.Narrative
import spock.lang.See
import spock.lang.Specification

/**
 * @author oluna
 */

@Narrative('''
This unit test class is intended to test the License MAnager Process in the following scenarios:

- Request License
- Resubmit Request
- Manually Submit Request
- Apply License
- Delete License
''')

@See('https://support.transitionmanager.com/browse/TM-5966')
class LicenseManagerServiceIntegrationTests extends Specification {
	GrailsApplication		grailsApplication
	LicenseAdminService  	licenseAdminService
	LicenseManagerService	licenseManagerService

	private String testEmail = 'sample@sampleEmail.com'
	private String testRequestNote = 'Test request note'
	private ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	private PersonTestHelper  personTestHelper = new PersonTestHelper()
	private DateTime today = new DateTime().toDateMidnight().toDateTime()
	private DateTime tomorrow = today.plusDays(1)
	private Project 	project
	private Person 		adminPerson
	private UserLogin	adminUser

	void setup () {
		// Create and admin user to be able to login
		grailsApplication.config.tdstm.license = [
				enabled : true,
				request_email: "octavio.luna@gmail.com",
				key: '/Users/octavio/Documents/TDS/tranman/trunk/licensePublicP.key',
				password: '&5b4T#lNItA9^Zg'
		]
		licenseAdminService.initialize(true)

		project = projectTestHelper.createProject()
		adminPerson = personTestHelper.createStaff(project.owner)
		adminUser = personTestHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])

	}


	def '01. Test license loading' () {
		setup: 'first we generate a new license request'
			License licenseRequest = licenseAdminService.generateRequest(null, project.owner, testEmail, License.Environment.DEMO.toString(), project.id, testRequestNote)
			String encodedMessage  = licenseRequest.toEncodedMessage()

		when: "we load the encoded request it into the manager"
			LicensedClient licensedClient = licenseManagerService.loadRequest(encodedMessage)

		then: "the licensed client has the same parameters than the request"
			licenseRequest.id == licensedClient.id
			licenseRequest.email == licensedClient.email
			licenseRequest.websitename == licensedClient.websitename
			licenseRequest.hostName == licensedClient.hostName
			licenseRequest.requestNote == licensedClient.requestNote
			licenseRequest.requestDate?.time == licensedClient.requestDate?.time
			licenseRequest.environment == licensedClient.environment

	}

	def '02. Activate License' () {
		setup: 'first we generate a new license request and load it'
			License licenseRequest = licenseAdminService.generateRequest(null, project.owner, testEmail, License.Environment.DEMO.toString(), project.id, testRequestNote)
			String encodedMessage  = licenseRequest.toEncodedMessage()
			LicensedClient licensedClient = licenseManagerService.loadRequest(encodedMessage)

		when: 'we configure the license'
			licensedClient.type = License.Type.MULTI_PROJECT
			licensedClient.max  = 100
			licensedClient.activationDate = today.toDate()
			licensedClient.expirationDate = tomorrow.toDate()
			licensedClient.save()
			String licenseKeyPending = licenseManagerService.getLicenseKey(licensedClient.id)

		then: 'the license is pending'
			License.Status.PENDING == licensedClient.status
		and: 'the license key is blank'
			StringUtils.isBlank(licenseKeyPending)


		when: 'we activate it'
			licenseManagerService.activate(licensedClient.id)
			String licKey = licenseManagerService.getLicenseKey(licensedClient.id)

		then: 'license status is ACTIVE'
			License.Status.ACTIVE == licensedClient.status
		and: 'we have a new hash to activate the license in the Admin side'
			StringUtils.isNotBlank(licKey)

	}

	def '03. Revoke license' () {
		setup: 'first we generate a new license request and load it'
			License licenseRequest = licenseAdminService.generateRequest(null, project.owner, testEmail, License.Environment.DEMO.toString(), project.id, testRequestNote)
			String encodedMessage  = licenseRequest.toEncodedMessage()
			LicensedClient licensedClient = licenseManagerService.loadRequest(encodedMessage)

		when: 'we configure the license'
			licensedClient.type = License.Type.MULTI_PROJECT
			licensedClient.max  = 100
			licensedClient.activationDate = today.toDate()
			licensedClient.expirationDate = tomorrow.toDate()
			licensedClient.save()
			String licenseKeyPending = licenseManagerService.getLicenseKey(licensedClient.id)

		then: 'the license is pending'
			License.Status.PENDING == licensedClient.status
		and: 'the license key is blank'
			StringUtils.isBlank(licenseKeyPending)


		when: 'we revoke it'
			licenseManagerService.revoke(licensedClient.id)
			String licKey = licenseManagerService.getLicenseKey(licensedClient.id)

		then: 'license status is ACTIVE'
			License.Status.TERMINATED == licensedClient.status
		and: 'the license key is blank'
			StringUtils.isBlank(licKey)

	}

	def '04. Modify License' () {

	}
}
