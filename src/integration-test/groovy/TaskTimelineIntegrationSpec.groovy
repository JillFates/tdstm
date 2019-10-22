import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.tasks.WsTimelineController
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class TaskTimelineIntegrationSpec extends  Specification {
	@Shared
	WsTimelineController wsTimelineController
	
	@Shared
	test.helper.MoveEventTestHelper moveEventTestHelper = new test.helper.MoveEventTestHelper()

	@Shared
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()

	@Shared
	test.helper.TaskTestHelper taskTestHelper = new test.helper.TaskTestHelper()
	
	@Shared
	Project project
	
	@Shared
	MoveEvent moveEvent
	
	@Shared
	Date now

	void setup() {
		project = projectTestHelper.createProject()

		moveEvent = moveEventTestHelper.createMoveEvent(project)
		
		now = TimeUtil.nowGMT().clearTime()
	}

	void "1. Test timeline method with nothing selected"() {
		when: 'Testing timeline method with nothing selected'
			Map results = wsTimeLineController.timeline()
		then: 'We get an empty result set'
			// we should get an error saying there is no move event selected
	}
	
	void "2. Test simple timeline"() {
		when: 'Calling the timeline method with a simple event'
			moveEvent = moveEventTestHelper.createMoveEvent(project)
			def task1 = taskTestHelper.createTask(null, project)
			def task2 = taskTestHelper.createTask(null, project)
			def task3 = taskTestHelper.createTask(null, project)
			def dep1 = taskTestHelper.createDependency(task1, task2)
			def dep2 = taskTestHelper.createDependency(task2, task3)
			def dep3 = taskTestHelper.createDependency(task1, task3)
			Map results = wsTimeLineController.timeline(moveEvent.id)
		then: 'We get a map of correct timeline results'
			// check that the data is correctly converted into a timeline with the correct features
	}
	
	void "3. Test timeline method with cycles"() {
		when: 'Calling the timeline method for an event with cycles'
			moveEvent = moveEventTestHelper.createMoveEvent(project)
			def task1 = taskTestHelper.createTask(null, project)
			def task2 = taskTestHelper.createTask(null, project)
			def dep1 = taskTestHelper.createDependency(task1, task2)
			def dep2 = taskTestHelper.createDependency(task2, task1)
			Map results = wsTimeLineController.timeline(moveEvent.id)
		then: 'We get an error explaining that there are cycles and the cycles should appear in the list'
			// cyclical error
	}

	void "4. Test recalculation"() {
		when: 'Calling the timeline method with the recalculation flag'
			moveEvent = moveEventTestHelper.createMoveEvent(project)
			moveEvent.actualStartTime = new Date('2000-01-01T02:00:00Z')
			def task1 = taskTestHelper.createTask(null, project)
			def task2 = taskTestHelper.createTask(null, project)
			def task3 = taskTestHelper.createTask(null, project)
			def task4 = taskTestHelper.createTask(null, project)
			task1.duration = 60
			task2.duration = 60
			task3.duration = 120
			task4.duration = 30
			def dep1 = taskTestHelper.createDependency(task1, task2)
			def dep2 = taskTestHelper.createDependency(task2, task3)
			def dep3 = taskTestHelper.createDependency(task1, task3)
			def dep4 = taskTestHelper.createDependency(task3, task4)
			Map results = wsTimeLineController.timeline(moveEvent.id, mode:'R')
		then: 'The tasks have new estimated starts/ends'
			// check the result tasks for new starts and ends
	}


	void "5. Test timeline with existing start dates that don't work"() {
		when: 'Calling the timeline method with no tasks with start dates'
			def task1 = taskTestHelper.createTask(null, project)
			def task2 = taskTestHelper.createTask(null, project)
			def task3 = taskTestHelper.createTask(null, project)
			task1.duration = 60
			task2.duration = 60
			task3.duration = 60
			task1.actStart = new Date('2000-01-01T02:00:00Z')
			task2.actStart = new Date('2000-01-01T02:00:00Z')
			task3.actStart = new Date('2000-01-01T02:00:00Z')
			def dep1 = taskTestHelper.createDependency(task1, task2)
			def dep2 = taskTestHelper.createDependency(task2, task3)
			def dep3 = taskTestHelper.createDependency(task1, task3)
			Map results = wsTimeLineController.timeline(moveEvent.id)
		then: 'We get a list of map results'
			// check the timeline results have the correct estimated times
		
	}
	
	void "6. Test empty event"() {
		when: 'Calling the timeline method with no tasks'
		moveEvent = moveEventTestHelper.createMoveEvent(project)
		Map results = wsTimeLineController.timeline(moveEvent.id)
		then: 'We get an empty timeline'
		// nothing
	}
}