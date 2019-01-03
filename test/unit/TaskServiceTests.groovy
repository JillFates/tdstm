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
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.WorkflowTransition
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.SequenceService
import net.transitionmanager.service.TaskService
import org.joda.time.DateTime
import spock.lang.Specification
import test.AbstractUnitSpec

@SuppressWarnings('unused')
@TestFor(TaskService)
@Mock([AssetEntity, AssetComment, CommentNote, TaskDependency, RoleType, PartyRelationshipService, Person, WorkflowTransition])
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
		task.dateResolved == null

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
		task.dateResolved != null

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
		null == task.dateResolved
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

	def 'Test addTagFilteringToWhere with not filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [:]
		when: 'calling addTagFilteringToWhere as an ANY with tag OneTag'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
		then: 'the SQL should be as expected'
			'' == result
		and: 'the params should have no values'
			params.size() == 0
	}

	def 'Test addTagFilteringToWhere with single ANY filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [tag:'OneTag']
		when: 'calling addTagFilteringToWhere as an ANY with tag OneTag'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ANY +
				service.TAG_WHERE_ASSET_PROJECT +
				service.TAG_WHERE_SUBSELECT_ANY_IN +
				'))'
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have one value that matches'
			params.size() == 2
			['OneTag'] == params['tagNameList']
			5L == params['projectId']
	}

	def 'Test addTagFilteringToWhere with multipe ANY filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [ tag: ['Red','Blue'], tagMatch: 'ANY' ]

		when: 'calling addTagFilteringToWhere'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ANY +
				service.TAG_WHERE_ASSET_PROJECT +
				service.TAG_WHERE_SUBSELECT_ANY_IN +
				'))'
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have two values that matches'
			params.size() == 2
			['Red', 'Blue'] == params['tagNameList']
			5L == params['projectId']
	}

	def 'Test addTagFilteringToWhere with single ANY LIKE filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [ tag: ['R%'], tagMatch: 'ANY' ]

		when: 'calling addTagFilteringToWhere'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ANY +
				service.TAG_WHERE_ASSET_PROJECT +
				'taws.tag.name LIKE :tagName_1' +
				'))'
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have one value that matches'
			params.size() == 2
			'R%' == params['tagName_1']
			5L == params['projectId']
	}

	def 'Test addTagFilteringToWhere with multiple ANY LIKE filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [ tag: ['R%', 'B%'], tagMatch: 'ANY' ]

		when: 'calling addTagFilteringToWhere'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ANY +
				service.TAG_WHERE_ASSET_PROJECT +
				'taws.tag.name LIKE :tagName_1' +
				' OR taws.tag.name LIKE :tagName_2' +
				'))'
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have one value that matches'
			params.size() == 3
			'R%' == params['tagName_1']
			'B%' == params['tagName_2']
			5L == params['projectId']
	}

	def 'Test addTagFilteringToWhere with a mixture ANY EXACT and LIKE filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [ tag: ['Red', 'R%', 'Blue', 'G%'], tagMatch: 'ANY' ]

		when: 'calling addTagFilteringToWhere'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ANY +
				service.TAG_WHERE_ASSET_PROJECT +
				'taws.tag.name IN (:tagNameList)' +
				' OR taws.tag.name LIKE :tagName_1' +
				' OR taws.tag.name LIKE :tagName_2' +
				'))'
			// println 'result:   ' + result
			// println 'expected: ' + expected
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have one value that matches'
			params.size() == 4
			['Red', 'Blue'] == params['tagNameList']
			'R%' == params['tagName_1']
			'G%' == params['tagName_2']
			5L == params['projectId']
	}

/***
 ***
 ***/

	def 'Test addTagFilteringToWhere with single ALL filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [tag: 'OneTag', tagMatch: 'ALL']
		when: 'calling addTagFilteringToWhere as an ANY with tag OneTag'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ALL +
				service.TAG_WHERE_ASSET_PROJECT +
				'taws.tag.name IN (:tagNameList)' +
				') ' + service.TAG_WHERE_SUBSELECT_ALL_GROUPBY +
				')'
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have one value that matches'
			params.size() == 3
			['OneTag'] == params['tagNameList']
			5L == params['projectId']
			1L == params['tagListSize']
	}

	def 'Test addTagFilteringToWhere with multipe ALL filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [ tag: ['Red','Blue'], tagMatch: 'ALL' ]

		when: 'calling addTagFilteringToWhere'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ALL +
				service.TAG_WHERE_ASSET_PROJECT +
				'taws.tag.name IN (:tagNameList)' +
				') ' + service.TAG_WHERE_SUBSELECT_ALL_GROUPBY +
				')'
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have two values that matches'
			params.size() == 3
			['Red', 'Blue'] == params['tagNameList']
			5L == params['projectId']
			2L == params['tagListSize']
	}

	def 'Test addTagFilteringToWhere with single ALL LIKE filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [ tag: ['R%'], tagMatch: 'ALL' ]

		when: 'calling addTagFilteringToWhere'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ALL +
				service.TAG_WHERE_ASSET_PROJECT +
				'taws.tag.name LIKE :tagName_1' +
				') ' + service.TAG_WHERE_SUBSELECT_ALL_GROUPBY +
				')'
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have one value that matches'
			params.size() == 3
			'R%' == params['tagName_1']
			5L == params['projectId']
			1L == params['tagListSize']
	}

	def 'Test addTagFilteringToWhere with multiple ALL LIKE filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [ tag: ['R%', 'B%'], tagMatch: 'ALL' ]

		when: 'calling addTagFilteringToWhere'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ALL +
				service.TAG_WHERE_ASSET_PROJECT +
				'taws.tag.name LIKE :tagName_1' +
				' OR taws.tag.name LIKE :tagName_2' +
				') ' + service.TAG_WHERE_SUBSELECT_ALL_GROUPBY +
				')'
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have one value that matches'
			params.size() == 4
			'R%' == params['tagName_1']
			'B%' == params['tagName_2']
			5L == params['projectId']
			2L == params['tagListSize']
}

	def 'Test addTagFilteringToWhere with a mixture ALL EXACT and LIKE filter'() {
		setup:
			String where = ''
			Map params = [:]
			Map filter = [ tag: ['Red', 'R%', 'Blue', 'G%'], tagMatch: 'ALL' ]

		when: 'calling addTagFilteringToWhere'
			String result = service.addTagFilteringToWhere(filter, params, where, 5L)
			String expected =
				'a.id IN (' +
				service.TAG_WHERE_SUBSELECT_ALL +
				service.TAG_WHERE_ASSET_PROJECT +
				'taws.tag.name IN (:tagNameList)' +
				' OR taws.tag.name LIKE :tagName_1' +
				' OR taws.tag.name LIKE :tagName_2' +
				') ' + service.TAG_WHERE_SUBSELECT_ALL_GROUPBY +
				')'
			println 'result:   ' + result
			println 'expected: ' + expected
		then: 'the SQL should be as expected'
			expected == result
		and: 'the params should have one value that matches'
			params.size() == 5
			['Red', 'Blue'] == params['tagNameList']
			'R%' == params['tagName_1']
			'G%' == params['tagName_2']
			5L == params['projectId']
			4L == params['tagListSize']
	}

	def 'Test createTaskFromSpec with workflow'() {
		setup:
			service.sequenceService = Mock(SequenceService)

			// Stubbing the read method of the workflow transition
			GroovySpy(WorkflowTransition, global:true)
			WorkflowTransition.read(_) >> new WorkflowTransition()

		when:
			def recipeId = 1
			Person whom = new Person(firstName: 'Robin', lastName: 'Banks')
			Project project = AbstractUnitSpec.buildMockProject()
			def taskList = [:]
			def taskSpec = [:]
			List<Person> projectStaff = []
			def settings = [
					  clientId: 1,
					  publishTasks: true,
					  project: project
			]
			def exceptions = new StringBuilder()
			def workflow = [
					  workflow_transition_id: 1,
					  category              : 'Category',
					  plan_start_time       : DateTime.newInstance().toDate(),
					  plan_completion_time  : DateTime.newInstance().plusDays(1).toDate(),
					  duration              : 1,
					  duration_scale        : TimeScale.D
			]
			def asset = null
			AssetComment task = service.createTaskFromSpec(
					  recipeId, whom, taskList, taskSpec, projectStaff, settings, exceptions, workflow, asset)

		then: 'we get a new task object'
			task != null

		and: 'the workflow transition is obtained (currently a mock object)'
			task.workflowTransition != null

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
