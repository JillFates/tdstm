import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import net.transitionmanager.command.DataviewApiParamsCommand
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.ErrorHandlerService
import net.transitionmanager.service.SecurityService
import spock.lang.Specification

@TestFor(DataviewController)
@TestMixin(GrailsUnitTestMixin)
class DataviewControllerSpec extends Specification {

	static doWithSpring = {
		springSecurityService(SpringSecurityService)

		securityService(SecurityService) {
			grailsApplication = ref('grailsApplication')
			springSecurityService = ref('springSecurityService')
		}
	}

	void 'test can request an Dataview only by its ID'() {

		setup:
			// tell Spring to behave as if the user has the required role(s)
			SpringSecurityUtils.metaClass.static.ifAllGranted = { String role ->
				return true
			}

			SpringSecurityUtils.metaClass.static.doWithAuth = { String role, Closure closure ->
				closure.call()
			}

			controller.dataviewService = Mock(DataviewService) {
				query(_, _, _) >> { Project project, Dataview dataview, DataviewApiParamsCommand apiParamsCommand ->
					return [
						:
					]
				}
			}

			controller.securityService = Mock(SecurityService)
			controller.errorHandlerService = Mock(ErrorHandlerService)

		when:
			controller.request.method = 'GET'
			params.id = '1'

			SpringSecurityUtils.doWithAuth('superuser'){
				controller.data()
			}


		then:
			response.text == 'qweqwe'

		cleanup:
			SpringSecurityUtils.metaClass = null
	}
}
