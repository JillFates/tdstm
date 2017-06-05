import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.CommentNote
import com.tds.asset.TaskDependency
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.TimeScale
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.RoleType
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.TaskService
import spock.lang.Specification

@SuppressWarnings('unused')
@TestFor(TaskService)
@Mock([AssetEntity, AssetComment, CommentNote, TaskDependency, RoleType, PartyRelationshipService, Person])
class TaskServiceTests extends Specification {

	void testCompareStatus() {
		// Groovy compiler doesn't like the -1 parameter unless in parens
		expect:
		-1 == service.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.COMPLETED)
		0 == service.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.STARTED)
		1 == service.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.READY)
		-1 == service.compareStatus(AssetCommentStatus.STARTED, null)
		1 == service.compareStatus(null, AssetCommentStatus.READY)
		0 == service.compareStatus(null, null)
	}

	void testSetTaskStatus() {
		when:
		Person whom = new Person(firstName: 'Robin', lastName: 'Banks')

		TestAssetComment task = new TestAssetComment(previousStatus: AssetCommentStatus.PENDING)
		task = service.setTaskStatus(task, AssetCommentStatus.STARTED, whom)

		then:
		task.actStart != null
		task.assignedTo != null
		AssetCommentStatus.STARTED == task.status
		task.actFinish == null
		task.isResolved == 0

		when:
		// Test bumping status to COMPLETED after STARTED
		task.previousStatus = task.status
		service.setTaskStatus(task, AssetCommentStatus.COMPLETED, whom)

		then:
		task.actStart != null
		task.actFinish != null
		task.assignedTo != null
		task.resolvedBy != null
		AssetCommentStatus.COMPLETED == task.status
		task.isResolved == 1

		when:
		// Test reverting status TO STARTED from COMPLETED
		def prevStarted = task.actStart
		task.previousStatus = task.status
		service.setTaskStatus(task, AssetCommentStatus.STARTED, whom)

		then:
		task.actStart != null
		task.actFinish == null
		task.assignedTo != null
		task.resolvedBy == null
		AssetCommentStatus.STARTED == task.status
		0 == task.isResolved
	}

	void testGetMoveEventRunbookRecipe() {
		def text = "[ tasks: [ [id: 1000, description: 'Start' ] ] ]"
		def me = new MoveEvent(runbookRecipe: text)

		def recipe = service.getMoveEventRunbookRecipe(me)
		def task = recipe[0].tasks[0]

		// See that it handles not getting an event as well
		expect:
		recipe
		1000 == task.id
		'Start' == task.description
		service.getMoveEventRunbookRecipe(null) == null
	}

	void testSetTaskDuration() {
		def task = new AssetComment(assetEntity: new AssetEntity())

		def msg
		def label

		when:
		label = 'Basic integer test'
		msg = service.setTaskDuration(task, 15)
		then:
		msg == null
		checkDurations(label, task, 15, TimeScale.M)

		when:
		label = 'Simple Time and Scale test'
		resetTaskDuration(task)
		msg = service.setTaskDuration(task, '3h')
		then:
		msg == null
		checkDurations(label, task, 3, TimeScale.H)

		when:
		label = 'Invalid Scale test for simple type'
		resetTaskDuration(task)
		msg = service.setTaskDuration(task, '3z')
		then:
		msg != null
		println "Error was $msg"

		when:
		label = 'Indirect reference with Integer'
		resetTaskDuration(task)
		task.assetEntity.custom1 = 50
		msg = service.setTaskDuration(task, '#custom1')
		then:
		msg == null
		checkDurations(label, task, 50, TimeScale.M)

		when:
		label = 'Indirect reference with String number'
		resetTaskDuration(task)
		task.assetEntity.custom1 = '10'
		msg = service.setTaskDuration(task, '#custom1')
		then:
		msg == null
		checkDurations(label, task, 10, TimeScale.M)

		when:
		label = 'Indirect reference with String number+scale'
		resetTaskDuration(task)
		task.assetEntity.custom1 = '5w'
		msg = service.setTaskDuration(task, '#custom1')
		then:
		msg == null
		checkDurations(label, task, 5, TimeScale.W)

		when:
		label = 'Indirect using default time'
		resetTaskDuration(task)
		task.assetEntity.custom1 = null
		msg = service.setTaskDuration(task, '#custom1, 30')
		then:
		msg == null
		checkDurations(label, task, 30, TimeScale.M)

		when:
		label = 'Indirect using default time+scale'
		resetTaskDuration(task)
		task.assetEntity.custom1 = null
		msg = service.setTaskDuration(task, '#custom1,10h')
		then:
		msg == null
		checkDurations(label, task, 10, TimeScale.H)
	}

	void testSetTaskDurationInvalidProperty() {
		when:
		def task = new AssetComment(assetEntity: new AssetEntity())
		resetTaskDuration(task)
		task.assetEntity.custom1 = null
		String msg = service.setTaskDuration(task, '#fubar,10h')

		then:
		msg.contains "Indirect duration '#fubar,10h' error"
	}

	void testSetTaskDurationInvalidNumber() {
		when:
		def task = new AssetComment(assetEntity: new AssetEntity())
		resetTaskDuration(task)
		task.assetEntity.custom1 = 10
		String msg = service.setTaskDuration(task, '#fubar, abc')

		then:
		msg == "Unrecognized duration value '#fubar, abc'"
	}

	// Helper method used to check a task duration settings
	private boolean checkDurations(String label, AssetComment task, int duration, TimeScale scale) {
		assert duration == task.duration
		assert scale == task.durationScale
		true
	}

	// Helper method that clears out the duration properties of a task
	private void resetTaskDuration(task) {
		task.duration = null
		task.durationScale = null
	}
}

class TestAssetComment extends AssetComment {
	String previousStatus

	def getPersistentValue(String fieldName) {
		if ('status' == fieldName) {
			return previousStatus
		}
	}
}
