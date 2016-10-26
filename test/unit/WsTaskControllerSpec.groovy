import grails.test.mixin.TestFor
import net.transitionmanager.service.TaskService
import test.AbstractUnitSpec

@TestFor(WsTaskController)
class WsTaskControllerSpec extends AbstractUnitSpec {

	void testPublish() {
		given:
		boolean called = false
		String idExpected = 213
		int expectedValue = 2

		controller.taskService = new TaskService() {
			def publish(taskId) {
				assert taskId == taskId
				called = true
				expectedValue
			}
		}

		when:
		controller.params.id = idExpected
		controller.publish()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 1
		data.tasksUpdated == expectedValue
	}

	void testUnpublish() {
		given:
		boolean called = false
		String idExpected = 2134
		int expectedValue = 1

		controller.taskService = new TaskService() {
			def unpublish(taskId) {
				assert taskId == taskId
				called = true
				expectedValue
			}
		}

		when:
		controller.params.id = idExpected
		controller.unpublish()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 1
		data.tasksUpdated == expectedValue
	}

	void testDeleteBatch() {
		given:
		boolean called = false
		String idExpected = 2134

		controller.taskService = new TaskService() {
			def deleteBatch(taskBatchId) {
				assert taskBatchId == idExpected
				called = true
				null
			}
		}

		when:
		controller.params.id = idExpected
		controller.deleteBatch()

		then:
		called
		assertSuccessJson controller.response
	}

	void testGenerateTasks() {
		given:
		boolean called = false
		def contextIdExpected = 'contextId'
		def recipeIdExpected = 'recipeId'
		boolean deletePreviousExpected = true
		boolean useWIPExpected = false
		boolean autoPublishExpected = true
		int jobIdExpected = 1357

		controller.taskService = new TaskService() {
			Map initiateCreateTasksWithRecipe(String contextId, String recipeId, Boolean deletePrevious,
			                                  Boolean useWIP, Boolean publishTasks) {
				assert contextId == contextIdExpected
				assert recipeId == recipeIdExpected
				assert deletePrevious == deletePreviousExpected
				assert useWIP == useWIPExpected
				assert publishTasks == autoPublishExpected
				called = true
				[jobId: jobIdExpected]
			}
		}

		when:
		controller.params.contextId = contextIdExpected
		controller.params.recipeId = recipeIdExpected
		controller.params.deletePrevious = deletePreviousExpected.toString()
		controller.params.useWIP = useWIPExpected.toString()
		controller.params.autoPublish = autoPublishExpected.toString()
		controller.generateTasks()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 1
		data.jobId == jobIdExpected
	}

	void testFindTaskBatchByRecipeAndContext() {
		given:
		boolean called = false
		String recipeIdExpected = 'recipeId'
		String contextIdExpected = 'contextId'
		String logsExpected = 'logs'
		def serviceReturn = 'regresar'

		controller.taskService = new TaskService() {
			def findTaskBatchByRecipeAndContext(recipeId, contextId, includeLogs) {
				assert recipeId == recipeIdExpected
				assert contextId == contextIdExpected
				assert includeLogs == logsExpected
				called = true
				serviceReturn
			}
		}

		when:
		controller.params.recipeId = recipeIdExpected
		controller.params.contextId = contextIdExpected
		controller.params.logs = logsExpected
		controller.findTaskBatchByRecipeAndContext()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 1
		data.taskBatch == serviceReturn
	}

	void testListTaskBatches() {
		given:
		boolean called = false
		String recipeIdExpected = 'recipeIdExpected'
		String limitDaysExpected = 'limitDaysExpected'
		def serviceReturn = '1+2+3+4+5+6'

		controller.taskService = new TaskService() {
			def listTaskBatches(recipeId, limitDays) {
				assert recipeId == recipeIdExpected
				assert limitDays == limitDaysExpected
				called = true
				serviceReturn
			}
		}

		when:
		controller.params.recipeId = recipeIdExpected
		controller.params.limitDays = limitDaysExpected
		controller.listTaskBatches()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 1
		data.list == serviceReturn
	}

	void testRetrieveTaskBatch() {
		given:
		boolean called = false
		String idExpected = 2134
		def serviceReturn = 'flurbfld'

		controller.taskService = new TaskService() {
			def getTaskBatch(taskBatchId) {
				assert taskBatchId == idExpected
				called = true
				serviceReturn
			}
		}

		when:
		controller.params.id = idExpected
		controller.retrieveTaskBatch()

		then:
		called
		assertSuccessJson controller.response

		when:
		def data = controller.response.json?.data

		then:
		data
		data.size() == 1
		data.taskBatch == serviceReturn
	}

	void testTaskReset() {
		given:
		boolean called = false
		String idExpected = 2134

		controller.taskService = new TaskService() {
			def resetTasksOfTaskBatch(taskBatchId) {
				assert taskBatchId == idExpected
				called = true
				'I can return anythiiiiiiiiiiiiiing  ....    wheeeeeeeeeeeeeeee'
			}
		}

		when:
		controller.params.id = idExpected
		controller.taskReset()

		then:
		called
		assertSuccessJson controller.response
	}

	void testRetrieveTasksOfTaskBatch() {
		given:
		boolean called = false
		String idExpected = 2134
		List<Map> serviceReturn = [[a: 5, b: 'beeee', z: 'r'], [pi: 3.14159265]]

		controller.taskService = new TaskService() {
			List<Map<String, Object>> getTasksOfBatch(String taskBatchId) {
				assert taskBatchId == idExpected
				called = true
				serviceReturn
			}
		}

		when:
		controller.params.id = idExpected
		controller.retrieveTasksOfTaskBatch()

		then:
		called
		assertSuccessJson controller.response

		when:
		def tasks = controller.response.json?.data?.tasks

		then:
		tasks
		tasks.size() == 2
		tasks[0].size() == 3
		tasks[0].keySet().sort() == ['a', 'b', 'z']
		tasks[1].size() == 1
		tasks[1].keySet().first() == 'pi'
	}
}
