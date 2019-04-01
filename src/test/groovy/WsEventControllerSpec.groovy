import grails.test.mixin.TestFor
import net.transitionmanager.move.WsEventController
import net.transitionmanager.service.EventService
import test.AbstractUnitSpec
import spock.lang.Ignore

@Ignore
@TestFor(WsEventController)
class WsEventControllerSpec extends AbstractUnitSpec {

	void testListEventsAndBundles() {
		given:
		boolean called = false

		controller.eventService = new EventService() {
			def listEventsAndBundles() {
				called = true
				[1, 2, 5] // taking advantage of untyped service method for now
			}
		}

		when:
		controller.listEventsAndBundles()

		then:
		called
		assertSuccessJson controller.response
	}

	void testListBundles() {
		given:
		boolean called = false
		String actualEventId = 'actual.event.id'
		String actualUseForPlanning = 'use.for.planning'

		controller.eventService = new EventService() {
			def listBundles(eventId, useForPlanning) {
				assert eventId == actualEventId
				assert useForPlanning == actualUseForPlanning
				called = true
				[1, 2, 5] // taking advantage of untyped service method for now
			}
		}

		when:
		controller.params.id = actualEventId
		controller.params.useForPlanning = actualUseForPlanning
		controller.listBundles()

		then:
		called
		assertSuccessJson controller.response

		when:
		def list = controller.response.json?.data?.list

		then:
		list
		list.size() == 3
		list.containsAll([1, 2, 5])
	}
}
