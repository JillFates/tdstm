import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tds.asset.*
import groovy.mock.interceptor.*
import grails.test.GrailsUnitTestCase
import org.apache.log4j.* 

/**
 * Unit test cases for the TaskService class
 */
class TaskServiceTests extends GrailsUnitTestCase {
	
	def taskService = new TaskService()
	def log
	
	void setUp() {
		// add the super call to avoid the "NullPointerException: Cannot invoke method containsKey() on null object" when calling mockDomain 
		super.setUp() 
		
		// build a logger...
        BasicConfigurator.configure() 
        LogManager.rootLogger.level = Level.DEBUG
        log = LogManager.getLogger("TaskService")

        // use groovy metaClass to put the log into your class
        TaskService.class.metaClass.getLog << {-> log}

		// Perform some IOC on the service that is necessary
		taskService.securityService = new SecurityService()
		
		// The domains to mock
		mockDomain AssetComment
		mockDomain CommentNote
		mockDomain TaskDependency
	}
	
	// @Test
	void testCompareStatus() {
		// Groovy compiler doesn't like the -1 parameter unless in parens
		assertEquals ( -1, taskService.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.DONE) )
		assertEquals  0, taskService.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.STARTED)
		assertEquals  1, taskService.compareStatus(AssetCommentStatus.STARTED, AssetCommentStatus.READY)
		assertEquals  ( -1, taskService.compareStatus(AssetCommentStatus.STARTED, null) )
		assertEquals  1, taskService.compareStatus(null, AssetCommentStatus.READY)
		assertEquals  0, taskService.compareStatus(null,null)
	}
	
	void testSetTaskStatus() {
		
		def mockTaskFactory = new MockFor(AssetComment)
		def mockTaskServiceFactory = new MockFor(TaskService)
		
		def whom = new Person(firstName:'Robin', lastName:'Banks')

		// dynamically add isDirty annd getPersistentValue methods because mockDomain doesn't do it - always dirty
		AssetComment.metaClass.isDirty { return true }
		AssetComment.metaClass.getPersistentValue { prop -> return prop=='status' ? previousStatus : 'UNDEF' }
		// Add a previousStatus property to the class to hold the PersistentValue for our testing (hack)
		AssetComment.metaClass.previousStatus = AssetCommentStatus.PENDING
		
		// fake out the Security Service so it returns hasRole based on what we want
		SecurityService.metaClass.hasRole { return false }
		
		def task = new AssetComment()
		
		// Test setting to STARTED 
		// mockTaskFactory.demand.getPersistentValue { prop -> return AssetCommentStatus.PENDING }
		task.previousStatus = AssetCommentStatus.PENDING
		task = taskService.setTaskStatus( task, AssetCommentStatus.STARTED, whom )
		assertNotNull task.actStart
		assertNotNull task.assignedTo
		assertEquals AssetCommentStatus.STARTED, task.status
		assertNull task.actFinish
		assertEquals 0, task.isResolved
		
		// Test bumping status to DONE after STARTED
		// task.metaClass.getPersistentValue { prop -> return AssetCommentStatus.STARTED }
		// mockTaskFactory.demand.getPersistentValue { prop -> return AssetCommentStatus.STARTED }
		task.previousStatus = task.status
		taskService.setTaskStatus( task, AssetCommentStatus.DONE, whom )
		assertNotNull task.actStart
		assertNotNull task.actFinish
		assertNotNull task.assignedTo
		assertNotNull task.resolvedBy
		assertEquals AssetCommentStatus.DONE, task.status
		assertEquals 1, task.isResolved
		

		// Test reverting status TO STARTED from DONE
		task.metaClass.getPersistentValue { prop -> return AssetCommentStatus.DONE }
		// mockTaskFactory.demand.getPersistentValue { prop -> return AssetCommentStatus.DONE }
		def prevStarted = task.actStart
		task.previousStatus = task.status
		taskService.setTaskStatus( task, AssetCommentStatus.STARTED, whom )
		assertNotNull task.actStart
		assertNull task.actFinish
		assertNotNull task.assignedTo
		assertNull task.resolvedBy
		assertEquals AssetCommentStatus.STARTED, task.status
		assertEquals 0, task.isResolved
		// assertTrue task.notes.list().size() > 0
		
	}
		
	void testGetMoveEventRunbookRecipe() {
		def text = "[ tasks: [ [ 'id':1000, 'description':'Start' ] ] ]"
		def mock = new MockFor(MoveEvent)
		def me = new MoveEvent(runbookRecipe:text)
		MoveEvent.metaClass.static.read = { id -> return id == 1 ? me : null }
		
		def recipe = taskService.getMoveEventRunbookRecipe(me)
		assertTrue recipe.size() > 0
		
		def task = recipe[0].tasks[0]
		assertEquals 1000, task.id
		assertEquals 'Start', task.description
		
		println recipe
		
		// See that it handles not getting an event as well
		assertNull taskService.getMoveEventRunbookRecipe(null)
		
	}
	
}