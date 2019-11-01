import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.ApplicationController
import net.transitionmanager.asset.ApplicationService
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.security.UserLogin
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import test.AbstractUnitSpec

class ApplicationControllerSpec extends AbstractUnitSpec implements DataTest, ControllerUnitTest<ApplicationController> {
	Closure doWithSpring() {
		{ ->
			authenticationTrustResolver(AuthenticationTrustResolverImpl)

			springSecurityService(SpringSecurityService) {
				authenticationTrustResolver = ref('authenticationTrustResolver')
			}

			securityService(SecurityService) {
				grailsApplication = ref('grailsApplication')
				springSecurityService = ref('springSecurityService')
			}

			userPreferenceService(UserPreferenceService) {
				springSecurityService = ref('springSecurityService')
			}
		}
	}

	void setupSpec(){
		mockDomains Application, UserLogin, Person
	}

	void setup(){
		controller.securityService = Mock(SecurityService)
	}

	void 'test the application delete'() {

		setup: 'a user with necessary permission is accessing the application delete action'
			ApplicationService mockApplicationService = Mock(ApplicationService)
			mockApplicationService.deleteApplication(_) >> { Application application ->
			}

			controller.applicationService = mockApplicationService

			String expectedAppId = RandomStringUtils.randomNumeric(6)
			String invalidAppId = RandomStringUtils.randomNumeric(6)
			String appName = RandomStringUtils.randomAlphabetic(15)

			Application.metaClass.static.get = { String id ->
				Application app = null
				if (expectedAppId == id) {
					app = new Application()
					app.id = NumberUtil.toLong(id)
					app.assetName = appName
				}
				return app
			}

			login()

		when: 'the user provides an invalid app id'
			controller.params.id = invalidAppId
			request.method = 'POST'
				controller.delete()
		then: 'the user receives an appropriate flash error message'
			flash.message == "Application not found with id ${params.id}"

		when: 'the user provides a valid app id'
			response.reset()
			controller.params.id = expectedAppId
			request.method = 'POST'
			controller.delete()
		then: 'the user receives a confirmation flash message that the application was deleted'
			flash.message == "Application $appName deleted"
	}
}
