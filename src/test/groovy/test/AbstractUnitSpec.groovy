package test

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.spring.TdsUserDetails
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.SpringSecurityService
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import org.grails.plugins.testing.AbstractGrailsMockHttpServletResponse
import org.quartz.DateBuilder
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

import static org.junit.Assert.fail
import static org.quartz.DateBuilder.newDate

abstract class AbstractUnitSpec extends Specification {
	protected static final String USERNAME = '__test_user__'
	protected static final Map<String, String> PERSON_DATA = [firstName: 'Hunter', middleName: 'S', lastName: 'Thompson']

	// TODO populate the test with random real data
	protected static final List<String> ROLE_USER_PERMISSIONS = [
		Permission.ArchitectureView,
		Permission.AssetDelete,
		Permission.AssetMenuView,
		Permission.DashboardMenuView,
		Permission.ProjectStaffList,
		Permission.ProjectStaffShow,
		Permission.RackMenuView,
		Permission.TaskGraphView ]

	void setup() {
		// it's assumed here that the test class is annotated with @TestFor or mixes in
		// a test helper class directly (e.g. with @TestMixin(ControllerUnitTestMixin))
		// and that these variables will resolve at runtime. Unfortunately
		// it's not possible to configure things here because the AST transforms don't
		// detect that another has already been applied.

		ApplicationContextHolder.instance.applicationContext = applicationContext

		//userPreferenceService.metaClass.session = session

		session.setAttribute('CURR_DT_FORMAT', TimeUtil.MIDDLE_ENDIAN)
		session.setAttribute('CURR_TZ', 'GMT')

		defineBeans {

			authenticationTrustResolver(AuthenticationTrustResolverImpl)

			springSecurityService(SpringSecurityService) {
				authenticationTrustResolver = ref('authenticationTrustResolver')
			}

			securityService(SecurityService) {
				grailsApplication = grailsApplication
				springSecurityService = ref('springSecurityService')
			}

			userPreferenceService(UserPreferenceService) {
				securityService = ref('securityService')
			}
		}

		RequestContextHolder.setRequestAttributes webRequest

		initAssociationIds()
	}

	void cleanup() {
		ApplicationContextHolder.instance.applicationContext = null
		RequestContextHolder.resetRequestAttributes()
		logout()
	}

	protected void logout() {
		SecurityContextHolder.clearContext()
	}

	protected <T> T save(T instance) {
		instance.save()
		if (instance.hasErrors()) {
			fail 'Validation error(s) saving ' + instance.getClass().simpleName + ': ' + GormUtil.allErrorsString(instance)
		}
		instance
	}

	protected boolean assertSuccessJson(AbstractGrailsMockHttpServletResponse response) {
		assert response.contentAsString
		assert response.json.status == 'success'
		true
	}

	// add @Mock(UserLogin) to the test class before calling this
	protected UserLogin login() {

		UserLogin userLogin = UserLogin.findByUsername(USERNAME)
		if (!userLogin) {
			Person person = Person.findWhere(PERSON_DATA) ?: save(new Person(PERSON_DATA))
			userLogin = save new UserLogin(username: USERNAME, active: 'Y', expiryDate: new Date() + 1000, person: person)
		}

		List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority('USER'))
		// Setup a user with a set of Permissions
		TdsUserDetails principal = new TdsUserDetails(USERNAME, 'password', true, true, true, true,
			authorities, userLogin.id, userLogin.personId, 'salt', ROLE_USER_PERMISSIONS)
		SecurityContextHolder.context.authentication = new TestingAuthenticationToken(principal, null, authorities)

		return userLogin
	}

	/**
	 * This Creates a Mock Project used in Unit Test
	 * @return a MockProject
	 */
	protected Project buildMockProject() {
		String projectName = 'projectName'
		String projectDescription = 'description'
		long projectId = 123
		String projectCode = 'projectCode'
		String projectClientName = 'projectClientName'
		long projectClientId = 321
		int completionDateYear = 2100
		int completionDateMonth = DateBuilder.DECEMBER
		int completionDateDay = 15
		Date completionDate = newDate()
			.inYear(completionDateYear)
			.inMonth(completionDateMonth)
			.onDay(completionDateDay)
			.build()
		completionDate.clearTime()

		Project project = new Project(
			name: projectName, projectCode: projectCode,
			completionDate: completionDate, description: projectDescription,
			client: new PartyGroup(name: projectClientName)
		)

		project.id = projectId
		project.client.id = projectClientId

		project
	}

	protected void initAssociationIds() {
		UserLogin.metaClass.getPersonId = { -> delegate.person?.id }
	}
}
