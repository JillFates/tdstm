import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import grails.test.GrailsMock
import grails.test.mixin.TestFor
import grails.plugin.springsecurity.SpringSecurityUtils

import net.transitionmanager.domain.AppMoveEvent
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.SecurityService

import org.apache.commons.lang3.RandomStringUtils

import spock.lang.Specification

import test.AbstractUnitSpec

@TestFor(ApplicationController)
@Mock([AssetEntityService, SecurityService, Application, UserLogin, AppMoveEvent])
class ApplicationControllerSpec extends AbstractUnitSpec {


	void "test the application delete"() {

		given:
			/* When mocking method implementations we need to provide the number
			of calls we're expecting to make (1 is the default). */
			int numberOfScenarios = 2

			GrailsMock mockAppMoveEvent = mockFor(AppMoveEvent)
			mockAppMoveEvent.demand.static.withNewSession(numberOfScenarios){Closure c ->
				//c.call()
			}

			// Setting up the AssetEntityService mock
			GrailsMock mockAssetEntityService = mockFor(AssetEntityService)
			mockAssetEntityService.demand.deleteAsset(numberOfScenarios){ AssetEntity asset ->
				// do nothing.
			}

			controller.assetEntityService = mockAssetEntityService.createMock()

			String expectedAppId = RandomStringUtils.randomNumeric(6)
			String invalidAppId = RandomStringUtils.randomNumeric(6)
			String appName = RandomStringUtils.randomAlphabetic(15)


			// Mocking an Application domain object to override the static get method.
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
		then:
			assertEquals(flash.message, "Application not found with id ${params.id}")

		when: "Valid app id"
			response.reset()
			controller.params.id = expectedAppId
			request.method = "POST"
			controller.delete()

		then:
			assertEquals(flash.message, "Application $appName deleted")
	}
}
