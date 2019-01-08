import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.i18n.Message
import net.transitionmanager.task.TaskFacade
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import spock.lang.Shared
import spock.lang.Specification
import test.helper.AssetCommentTestHelper
import test.helper.MoveEventTestHelper

@Integration
@Rollback
class TaskFacadeIntegrationSpec extends Specification {

	@Shared
	def grailsApplication
	ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	MoveEventTestHelper moveEventTestHelper = new MoveEventTestHelper()
	AssetCommentTestHelper assetCommentTestHelper = new AssetCommentTestHelper()

	Project project
	MoveEvent moveEvent
	AssetComment assetComment

	void setup() {
		project = projectTestHelper.createProject(null)
		moveEvent = moveEventTestHelper.createMoveEvent(project)
		assetComment = assetCommentTestHelper.createAssetComment(project, moveEvent)
	}

	void 'test different bean ids when getting prototype TaskFacade bean from SpringContext'() {
		setup: 'giving 2 task facade beans'
			TaskFacade taskFacade1 = getTaskFacadeBean()
			TaskFacade taskFacade2 = getTaskFacadeBean()
		expect: 'those beans are different event when they are constructed the using the same arguments'
			taskFacade1.hashCode() != taskFacade2.hashCode()
	}

	void 'test getting missing property on TaskFacade should throw a MissingPropertyException'() {
		setup: 'giving a task facade with an asset comment'
			TaskFacade taskFacade = getTaskFacadeBean()
		when: 'getting a non existing property'
			taskFacade.unknownField
		then: 'a MissingPropertyException should be thrown'
			MissingPropertyException e = thrown MissingPropertyException
			'No such property: unknownField' == e.message
	}

	void 'test task start using TaskFacade should start task and update status'() {
		setup: 'giving a task facade with an asset comment'
			TaskFacade taskFacade = getTaskFacadeBean()
		expect:
			!taskFacade.isStarted()
		when: 'staring the task'
			taskFacade.start()
		then: 'the task status should be updated accordingly'
			AssetCommentStatus.STARTED == taskFacade.status
		and: 'task isStarted flag should reflect true'
			taskFacade.isStarted()
	}

	void 'test task done using TaskFacade should complete the task and update status'() {
		setup: 'giving a task facade with an asset comment'
			TaskFacade taskFacade = getTaskFacadeBean()
		expect:
			!taskFacade.isDone()
		when: 'completing the task'
			taskFacade.done()
		then: 'the task status should be updated accordingly'
			AssetCommentStatus.COMPLETED == taskFacade.status
		and: 'task isDone flag should reflect true'
			taskFacade.isDone()
	}

	void 'test task error using TaskFacade should put the task on hold and add a new note indicating the error reason'() {
		setup: 'giving a task facade with an asset comment'
			TaskFacade taskFacade = getTaskFacadeBean()
		expect:
			!taskFacade.isOnHold()
		when: 'marking the task as in error'
			taskFacade.error('Test task in error')
		then: 'the task status should be updated accordingly'
			AssetCommentStatus.HOLD == taskFacade.status
		and: 'task notes should contain the error message passed as the error reason'
			taskFacade.notes.contains('Test task in error')
		and: 'task isOnHold flag should reflect true'
			taskFacade.isOnHold()
	}

	void 'test task lapsed using TaskFacade should put the task on hold and a new note with the lapsed i18n message should be added'() {
		setup: 'giving a task facade with an asset comment'
			TaskFacade taskFacade = getTaskFacadeBean()
		expect:
			!taskFacade.isOnHold()
		when: 'marking the task as lapsed'
			taskFacade.lapsed()
		then: 'the task status should be updated accordingly'
			AssetCommentStatus.HOLD == taskFacade.status
		and: 'task notes should contain the lapsed i18n message'
			taskFacade.notes.contains(getExpectedI18NMessage(Message.ApiActionTaskMessageLapsed))
		and: 'task isOnHold flag should reflect true'
			taskFacade.isOnHold()
	}

	void 'test task stalled using TaskFacade should put the task on hold and a new note with the stalled i18n message should be added'() {
		setup: 'giving a task facade with an asset comment'
			TaskFacade taskFacade = getTaskFacadeBean()
		expect:
			!taskFacade.isOnHold()
		when: 'marking the task as stalled'
			taskFacade.stalled()
		then: 'the task status should be updated accordingly'
			AssetCommentStatus.HOLD == taskFacade.status
		and: 'task notes should contain the stalled i18n message'
			taskFacade.notes.contains(getExpectedI18NMessage(Message.ApiActionTaskMessageStalled))
		and: 'task isOnHold flag should reflect true'
			taskFacade.isOnHold()
	}

	void 'test task timedout using TaskFacade should put the task on hold and a new note with the timed out i18n message should be added'() {
		setup: 'giving a task facade with an asset comment'
			TaskFacade taskFacade = getTaskFacadeBean()
		expect:
			!taskFacade.isOnHold()
		when: 'marking the task as timed out'
			taskFacade.timedOut()
		then: 'the task status should be updated accordingly'
			AssetCommentStatus.HOLD == taskFacade.status
		and: 'task notes should contain the time out i18n message'
			taskFacade.notes.contains(getExpectedI18NMessage(Message.ApiActionTaskMessageTimedout))
		and: 'task isOnHold flag should reflect true'
			taskFacade.isOnHold()
	}

	void 'test task duration using TaskFacade should return current task duration'() {
		setup: 'giving a task facade with an asset comment'
			TaskFacade taskFacade = getTaskFacadeBean()
		expect: 'task duration should be returned'
			0 == taskFacade.durationInMinutes()
	}

	/**
	 * Get a TaskFacade bean from context
	 * @return
	 */
	private TaskFacade getTaskFacadeBean() {
		return grailsApplication.getMainContext().getBean(TaskFacade.class, assetComment)
	}

	/**
	 * Get a i18n message from message source
	 * @param key
	 * @return
	 */
	private String getExpectedI18NMessage(String key) {
		MessageSource messageSource = grailsApplication.getMainContext().getBean(MessageSource.class)
		messageSource.getMessage(key, null, LocaleContextHolder.getLocale())
	}
}

