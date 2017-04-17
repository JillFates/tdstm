import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import grails.test.GrailsMock
import grails.test.mixin.TestFor

import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApplicationService
import net.transitionmanager.service.SecurityService

import org.apache.commons.lang3.RandomStringUtils

import spock.lang.Specification

import test.AbstractUnitSpec

@TestFor(ApplicationController)
@Mock([ApplicationService, SecurityService, Application, UserLogin])
class ApplicationControllerSpec extends AbstractUnitSpec {


	void "test the application delete"() {

		given: "An initial setup"
			int numberOfScenarios = 2
			GrailsMock mockApplicationService = mockFor(ApplicationService)
			mockApplicationService.demand.deleteApplication(numberOfScenarios){ Application application ->
			}

			controller.applicationService = mockApplicationService.createMock()

			String expectedAppId = RandomStringUtils.randomNumeric(6)
			String invalidAppId = RandomStringUtils.randomNumeric(6)
			String appName = RandomStringUtils.randomAlphabetic(15)

			GrailsMock mockApp = mockFor(Application)

			mockApp.demand.static.get(numberOfScenarios){String id ->
				Application app = null
				if(expectedAppId == id){
					app = new Application()
					app.id = NumberUtil.toLong(id)
					app.assetName = appName
				}
				return app
			}
			login()
		when: "Invalid App Id provided."
			controller.params.id = invalidAppId
			request.method = "POST"
			controller.delete()
		then: "The application reports not being able to locate the application."
			flash.message == "Application not found with id ${params.id}"

		when: "Valid app id"
			response.reset()
			controller.params.id = expectedAppId
			request.method = "POST"
			controller.delete()

		then: "The application deletes the application and returns a confirmation message."
			flash.message == "Application $appName deleted"
	}
}
