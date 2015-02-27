import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.TimeScale
import com.tds.asset.*
import groovy.mock.interceptor.*
import grails.test.GrailsUnitTestCase
import org.apache.log4j.* 
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the TaskService class
 */
@TestFor(TaskService)
@Mock([AssetComment, CommentNote, TaskDependency, RoleType, PartyRelationshipService, Person])
class TaskServiceTests extends Specification {
	
	def taskService
	def log
	
	void setup() {
		// add the super call to avoid the "NullPointerException: Cannot invoke method containsKey() on null object" when calling mockDomain 
		//super.setUp() 
		
		// build a logger...
		BasicConfigurator.configure() 
		LogManager.rootLogger.level = Level.DEBUG
		log = LogManager.getLogger("TaskService")

		taskService = new TaskService()
		
		// use groovy metaClass to put the log into your class
		TaskService.class.metaClass.getLog << {-> log}

		// Perform some IOC on the service that is necessary
		taskService.securityService = new SecurityService()
		
		// The domains to mock
	}
	
	// @Test
	void testCompareStatus() {
		// Groovy compiler doesn't like the -1 parameter unless in parens
		expect:
		-1 == taskService.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.DONE) 
		0 == taskService.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.STARTED)
		1 == taskService.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.READY)
		-1 == taskService.compareStatus(AssetCommentStatus.STARTED, null) 
		1 == taskService.compareStatus(null, AssetCommentStatus.READY)
		0 == taskService.compareStatus(null,null)
	}
	
	void testSetTaskStatus() {
		when:
			def whom = new Person(firstName:'Robin', lastName:'Banks')

			// Add a previousStatus property to the class to hold the PersistentValue for our testing (hack)
			AssetComment.metaClass.setProperty('previousStatus', AssetCommentStatus.PENDING)

			// fake out the Security Service so it returns hasRole based on what we want
			SecurityService.metaClass.hasRole = { return false }

			def taskTemplate = mockFor(AssetComment, true)

			def task = taskTemplate.createMock()

			// dynamically add isDirty and getPersistentValue methods because mockDomain doesn't do it - always dirty
			task.metaClass.isDirty { return true }
			task.metaClass.getPersistentValue { prop -> return prop=='status' ? previousStatus : 'UNDEF' }
			task.metaClass.isRunbookTask { return true }
			task.metaClass.addToNotes { note -> println "Note added: " + note}

			// Test setting to STARTED 
			task.previousStatus = AssetCommentStatus.PENDING
			task = taskService.setTaskStatus( task, AssetCommentStatus.STARTED, whom )
		then:
			task.actStart != null
			task.assignedTo != null
			AssetCommentStatus.STARTED == task.status
			task.actFinish  == null
			task.isResolved == 0

		when:
			// Test bumping status to DONE after STARTED
			// task.metaClass.getPersistentValue { prop -> return AssetCommentStatus.STARTED }
			task.previousStatus = task.status
			taskService.setTaskStatus( task, AssetCommentStatus.DONE, whom )
		then:
			task.actStart != null
			task.actFinish != null
			task.assignedTo != null
			task.resolvedBy != null
			AssetCommentStatus.DONE == task.status
			task.isResolved == 1
		
		when:
			// Test reverting status TO STARTED from DONE
			task.metaClass.getPersistentValue { prop -> return AssetCommentStatus.DONE }
			def prevStarted = task.actStart
			task.previousStatus = task.status
			taskService.setTaskStatus( task, AssetCommentStatus.STARTED, whom )
		then:
			task.actStart != null
			task.actFinish == null
			task.assignedTo != null
			task.resolvedBy == null
			AssetCommentStatus.STARTED == task.status
			0 == task.isResolved
	}
		
	void testGetMoveEventRunbookRecipe() {
		def text = "[ tasks: [ [ 'id':1000, 'description':'Start' ] ] ]"
		def mock = new MockFor(MoveEvent)
		def me = new MoveEvent(runbookRecipe:text)
		MoveEvent.metaClass.static.read = { id -> return id == 1 ? me : null }
		
		def recipe = taskService.getMoveEventRunbookRecipe(me)
		def task = recipe[0].tasks[0]
		
		println recipe
		
		// See that it handles not getting an event as well
		expect:
			recipe.size() > 0
			1000 == task.id
			'Start' == task.description
			taskService.getMoveEventRunbookRecipe(null) == null
	}
	
	// Helper method used to check a task duration settings
	boolean checkDurations(label, task, duration, scale) {
		def valid = true
		valid = valid && duration.equals(task.duration)
		valid = valid && scale.equals(task.durationScale)
		return valid
	}

	// Helper method that clears out the duration properties of a task
	void resetTaskDuration(task) {
		task.duration = null
		task.durationScale = null
	}

	void testSetTaskDuration() {
		def task = new AssetComment()
		task.assetEntity = new AssetEntity()

		def msg 
		def label

		when:
			label = 'Basic integer test'
			msg = taskService.setTaskDuration(task, 15)
		then:
			msg == null
			checkDurations(label, task, 15, TimeScale.M)

		when:
			label = 'Simple Time and Scale test'
			resetTaskDuration(task)
			msg = taskService.setTaskDuration(task, '3h')
		then:
			msg == null
			checkDurations(label, task, 3, TimeScale.H)

		when:
			label = 'Invalid Scale test for simple type'
			resetTaskDuration(task)
			msg = taskService.setTaskDuration(task, '3z')
		then:
			msg != null
			println "Error was $msg"

		when:
			label = 'Indirect reference with Integer'
			resetTaskDuration(task)
			task.assetEntity.custom1 = 50
			msg = taskService.setTaskDuration(task, '#custom1')
		then:
			msg == null
			checkDurations(label, task, 50, TimeScale.M)

		when:
			label = 'Indirect reference with String number'
			resetTaskDuration(task)
			task.assetEntity.custom1 = '10'
			msg = taskService.setTaskDuration(task, '#custom1')
		then:
			msg == null
			checkDurations(label, task, 10, TimeScale.M)

		when:
			label = 'Indirect reference with String number+scale'
			resetTaskDuration(task)
			task.assetEntity.custom1 = '5w'
			msg = taskService.setTaskDuration(task, '#custom1')
		then:
			msg == null
			checkDurations(label, task, 5, TimeScale.W)

		when:
			label = 'Indirect using default time'
			resetTaskDuration(task)
			task.assetEntity.custom1 = null
			msg = taskService.setTaskDuration(task, '#custom1, 30')
		then:
			msg == null
			checkDurations(label, task, 30, TimeScale.M)

		when:
			label = 'Indirect using default time+scale'
			resetTaskDuration(task)
			task.assetEntity.custom1 = null
			msg = taskService.setTaskDuration(task, '#custom1,10h')
		then:
			msg == null
			checkDurations(label, task, 10, TimeScale.H)

		when:
			label = 'Indirect with invalid property'
			resetTaskDuration(task)
			task.assetEntity.custom1 = null
			msg = taskService.setTaskDuration(task, '#fubar,10h')
		then:
			msg != null

		when:
			label = 'Indirect with invalid default'
			resetTaskDuration(task)
			task.assetEntity.custom1 = 10
			msg = taskService.setTaskDuration(task, '#fubar, abc')
		then:
			msg != null
	}

}