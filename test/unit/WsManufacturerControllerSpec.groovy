import grails.test.mixin.TestFor
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.ManufacturerService
import test.AbstractUnitSpec

@TestFor(WsManufacturerController)
class WsManufacturerControllerSpec extends AbstractUnitSpec {

	void testMerge() {
		given:
		boolean called = false
		String manufacturerToIdExpected = 'manuftoid'
		String manufacturerFromIdExpected = 'manufroid'

		controller.controllerService = new ControllerService() {
			void checkPermissionForWS(String perm) {
				assert perm == 'EditModel'
				called = true
			}
		}

		controller.manufacturerService = new ManufacturerService() {
			void merge(String manufacturerToId, String manufacturerFromId) {
				assert manufacturerToId == manufacturerToIdExpected
				assert manufacturerFromId == manufacturerFromIdExpected
			}
		}

		when:
		controller.params.id = manufacturerToIdExpected
		controller.params.fromId = manufacturerFromIdExpected
		controller.merge()

		then:
		called
		assertSuccessJson controller.response
	}
}
