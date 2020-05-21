import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.AssetOptionsService
import net.transitionmanager.common.SequenceService
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.Person
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.security.RoleType
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.CommentNote
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.TaskService
import org.quartz.Scheduler
import org.quartz.Trigger
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static net.transitionmanager.asset.AssetOptions.AssetOptionsType.TASK_CATEGORY

@ConfineMetaClassChanges([GormUtil])
class TaskServiceTests extends Specification implements ServiceUnitTest<TaskService>, DataTest{

	void setupSpec(){
		mockDomains AssetEntity, AssetComment, CommentNote, TaskDependency, RoleType, Person, Project, AssetOptions

		defineBeans {
			assetOptionsService(AssetOptionsService)

			applicationContextHolder(ApplicationContextHolder) { bean ->
				bean.factoryMethod = 'getInstance'
			}
		}
	}

	void setup(){
		//Overriding this so that in the CustomValidators.inList the calls to getConstraintValue don't blow up.
		GormUtil.metaClass.static.getConstraintValue = { Class clazz, String propertyName, String constraintName -> [] as Object }
		new AssetOptions(type: TASK_CATEGORY, value: 'general').save(flush: true, failOnError: true)

		service.partyRelationshipService = [staffHasFunction: { Project project, staffId, functionCodes -> false }] as PartyRelationshipService

		service.assetOptionsService = [
			taskCategories: { ->
				['general', 'discovery', 'analysis', 'design', 'planning', 'buildout', 'walkthru', 'premove', 'moveday', 'shutdown', 'physical', 'transport', 'startup', 'verify', 'postmove', 'closeout', 'learning'] }
		] as AssetOptionsService
	}

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
		setup:
			service.quartzScheduler = [
				scheduleJob: { Trigger trigger -> new Date() }
			] as Scheduler
		when:
			Person whom = new Person(firstName: 'Robin', lastName: 'Banks')

			Project project = new Project(
				name: 'projectName',
				projectCode: 'projectCode',
				completionDate: (new Date() + 5).clearTime(),
				description: 'projectDescription',
				client: new PartyGroup(name: 'client'),
				guid: StringUtil.generateGuid()
			).save(flush: true)

			AssetComment task = new AssetComment(
				comment: 'a comment',
				commentType: 'comment',
				project: project,
				status: 'Planned'
			).save(flush: true)

			task = service.setTaskStatus(task, AssetCommentStatus.STARTED, whom, true)

		then:
			task.actStart != null
			task.assignedTo != null
			AssetCommentStatus.STARTED == task.status
			task.actFinish == null
			task.dateResolved == null

		when:
			// Test bumping status to COMPLETED after STARTED
			service.setTaskStatus(task, AssetCommentStatus.COMPLETED, whom, true)

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
			Map filter = [tag: 'OneTag']
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
			Map filter = [tag: ['Red', 'Blue'], tagMatch: 'ANY']

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
			Map filter = [tag: ['R%'], tagMatch: 'ANY']

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
			Map filter = [tag: ['R%', 'B%'], tagMatch: 'ANY']

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
			Map filter = [tag: ['Red', 'R%', 'Blue', 'G%'], tagMatch: 'ANY']

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
			Map filter = [tag: ['Red', 'Blue'], tagMatch: 'ALL']

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
			Map filter = [tag: ['R%'], tagMatch: 'ALL']

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
			Map filter = [tag: ['R%', 'B%'], tagMatch: 'ALL']

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
			Map filter = [tag: ['Red', 'R%', 'Blue', 'G%'], tagMatch: 'ALL']

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

	def 'Test createTaskFromSpec'() {
		setup:
			service.sequenceService = Mock(SequenceService)
			RoleType role = new RoleType(id: 'SECURITY', type: 'SECURITY', level: 0)
			role.save(flush: true)

		when:
			def recipeId = 1
			Person whom = new Person(firstName: 'Robin', lastName: 'Banks')

			Project project = new Project(
				name: 'projectName',
				projectCode: 'projectCode',
				completionDate: (new Date() + 5).clearTime(),
				description: 'projectDescription',
				client: new PartyGroup(name: 'client'),
				guid: StringUtil.generateGuid()
			).save(flush: true)

			def taskList = [:]
			def taskSpec = [:]
			List<Person> projectStaff = []

			def settings = [
				clientId    : 1,
				publishTasks: true,
				project     : project
			]

			def exceptions = new StringBuilder()

			def asset = null
			AssetComment task = service.createTaskFromSpec(
				recipeId, whom, taskList, taskSpec, projectStaff, settings, exceptions, asset)

		then: 'we get a new task object'
			task != null

	}

	void 'Test tardy factor'() {
		expect: 'Test the resulting tardy factor for different durations.'
		'For durations between 0 and 5, tardy factor should be 5. '
		'For durations equal or bigger than 300, tardy factor should be 30.'
		'Any duration in between should give the 10 percentage integer value of the duration for the tardy factor.'
		service.computeTardyFactor(duration) == tardyFactor
		where:
		duration	| tardyFactor
		1 		| 5
		10   	| 5
		30      | 5
		50 	    | 5
		55 	    | 5
		60 	    | 6
		63 	    | 6
		69 	    | 6
		70 	    | 7
		80 	    | 8
		90 	    | 9
		100 	| 10
		200     | 20
		250     | 25
		300     | 30
		310     | 30
		350     | 30
		800     | 30
		2500    | 30
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
