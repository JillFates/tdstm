import grails.test.mixin.TestFor
import net.transitionmanager.common.WsProgressController
import net.transitionmanager.common.ProgressService
import test.AbstractUnitSpec
import spock.lang.Ignore

@Ignore
@TestFor(WsProgressController)
class WsProgressControllerSpec extends AbstractUnitSpec {

	void testRetrieveStatus() {
		given:
		boolean called = false
		def actualKey = 'actual.key'

		controller.progressService = new ProgressService() {
			Map get(key) {
				assert key == actualKey
				called = true
				[a: 1, b: 2, c: 5] // taking advantage of untyped service method for now
			}
		}

		when:
		controller.params.id = actualKey
		controller.retrieveStatus()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 3
		data.a == 1
		data.b == 2
		data.c == 5
	}

	void testRetrieveData() {
		given:
		boolean called = false
		String actualKey = 'actual.key'
		String actualDataKey = 'actual.data.key'

		controller.progressService = new ProgressService() {
			def getData(String key, dataKey) {
				assert key == actualKey
				assert dataKey == actualDataKey
				called = true
				[a: 'cluck', z: 'moo']
			}
		}

		when:
		controller.params.id = actualKey
		controller.params.dataKey = actualDataKey
		controller.retrieveData()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 2
		data.a == 'cluck'
		data.z == 'moo'
	}

	void testList() {
		given:
		boolean called = false

		when:
		controller.progressService = new ProgressService() {
			List<Map> list() {
				called = true
				[[a: 1, b: 2], [c: 42]]
			}
		}

		controller.list()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 2
		data[0].size() == 2
		data[0].a == 1
		data[0].b == 2
		data[1].size() == 1
		data[1].c == 42
	}

	void testDemo() {
		given:
		boolean called = false

		controller.progressService = new ProgressService() {
			def demo() {
				called = true
				[key: 'Task-' + UUID.randomUUID()]
			}
		}

		when:
		controller.demo()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 1
		data.key.startsWith('Task-')
		data.key.length() == 41
		data.key.split('-').size() == 6
	}

	void testDemoFailed() {
		given:
		boolean called = false

		controller.progressService = new ProgressService() {
			def demoFailed() {
				called = true
				[key: 'Task-' + UUID.randomUUID()]
			}
		}

		when:
		controller.demoFailed()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 1
		data.key.startsWith('Task-')
		data.key.length() == 41
		data.key.split('-').size() == 6
	}
}
