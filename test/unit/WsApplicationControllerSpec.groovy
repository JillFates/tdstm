import grails.test.mixin.TestFor
import net.transitionmanager.service.ApplicationService
import test.AbstractUnitSpec
import spock.lang.Ignore

@Ignore
@TestFor(WsApplicationController)
class WsApplicationControllerSpec extends AbstractUnitSpec {

	void testListInBundle() {
		given:
		boolean called = false
		def actualBundleId = 'actual.bundle.id'

		controller.applicationService = new ApplicationService() {
			def listInBundle(bundleId) {
				assert bundleId == actualBundleId
				called = true
				[1, 2, 5] // taking advantage of untyped service method for now
			}
		}

		when:
		controller.params.id = actualBundleId
		controller.listInBundle()

		then:
		called
		assertSuccessJson controller.response
	}
}
