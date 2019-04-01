import grails.test.mixin.TestFor
import net.transitionmanager.common.WsSequenceController
import net.transitionmanager.service.SequenceService
import test.AbstractUnitSpec
import spock.lang.Ignore

@Ignore
@TestFor(WsSequenceController)
class WsSequenceControllerSpec extends AbstractUnitSpec {

	void testRetrieveNext() {
		given:
		boolean called = false
		Long contextIdExpected = 123
		String keyExpected = 'key'
		Integer maxTriesExpected = 10
		Integer next = -73

		controller.sequenceService = new SequenceService() {
			Integer next(Long contextId, String key, Integer maxTries=10) {
				assert contextId == contextIdExpected
				assert key == keyExpected
				assert maxTries == maxTriesExpected

				called = true
				next
			}
		}

		when:
		controller.params.contextId = contextIdExpected.toString()
		controller.params.name = keyExpected
		controller.retrieveNext()

		then:
		called
		controller.response.json.seq == next
	}
}
