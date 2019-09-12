package net.transitionmanager.task

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentType
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import spock.lang.Shared
import spock.lang.Specification
import test.helper.ApplicationTestHelper
import test.helper.AssetCommentTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.ProjectTestHelper

import java.text.SimpleDateFormat

import static com.tdsops.tm.enums.domain.AssetCommentStatus.STARTED

@Integration
@Rollback
class TaskIntegrationSpec extends Specification {

	@Shared
	ProjectTestHelper projectTestHelper

	@Shared
	MoveBundleTestHelper moveBundleTestHelper

	@Shared
	AssetCommentTestHelper assetCommentTestHelper

	@Shared
	ApplicationTestHelper applicationTestHelper

	@Shared
	Project project

	@Shared
	MoveBundle moveBundle

	@Shared
	SimpleDateFormat formatter = new SimpleDateFormat('MM/dd/yyyy hh:mm')

	@Shared
	String aDay = '06/22/2018'

	void setup() {
		projectTestHelper = new ProjectTestHelper()
		assetCommentTestHelper = new AssetCommentTestHelper()
		moveBundleTestHelper = new MoveBundleTestHelper()
		applicationTestHelper = new ApplicationTestHelper()

		project = projectTestHelper.createProject(null)
		moveBundle = moveBundleTestHelper.createBundle(project, null)
	}

	void 'can create a Task instance and retrieve from database'() {

		given: 'A Task instance created in Database'
			Integer taskCount = Task.count()
			Integer assetCommentCount = AssetComment.count()

			Task task = new Task(
				project: project,
				taskNumber: 1,
				comment: 'Task 1',
				duration: 30,
				actStart: hourInDay('06:00'),
				status: STARTED
			).save(flush: true)

		expect: 'that instance can be retrieved by Task GORM finders'
			Task.count() == taskCount + 1
			AssetComment.count() == assetCommentCount + 1
			Task.findByProjectAndTaskNumber(project, 1).taskNumber == task.taskNumber
	}

	void 'can create a AssetComment instance and retrieve using Task domain'() {

		given: 'A Task instance created in Database'
			Integer taskCount = Task.count()
			Integer assetCommentCount = AssetComment.count()

			AssetComment task = new AssetComment(
				project: project,
				taskNumber: 1,
				comment: 'Task 1',
				commentType: AssetCommentType.TASK,
				duration: 30,
				actStart: hourInDay('06:00'),
				status: STARTED
			).save(flush: true)

		expect: 'that instance can be retrieved by Task GORM finders'
			Task.count() == taskCount + 1
			AssetComment.count() == assetCommentCount + 1
			AssetComment.findByProjectAndTaskNumber(project, 1).taskNumber == task.taskNumber
	}

	void 'can create a Task and AssetComment instances and retrieve them from database'() {

		given: 'A Task instance created in Database'
			Integer taskCount = Task.count()
			Integer assetCommentCount = AssetComment.count()

			Task task = new Task(
				project: project,
				taskNumber: 1,
				comment: 'Task 1',
				duration: 30,
				actStart: hourInDay('06:00'),
				status: STARTED
			).save(flush: true)

			AssetEntity device = applicationTestHelper.createApplication(AssetClass.DEVICE, project, moveBundle)
			AssetComment assetComment = assetCommentTestHelper.createComment(device, project, 'Comment I')

		expect: 'that instance can be retrieved by Task GORM finders'
			Task.count() == taskCount + 1
			Task.get(task.id).taskNumber == task.taskNumber
			Task.findByProjectAndTaskNumber(project, 1).taskNumber == task.taskNumber

		and: 'that instance can be retrieved by AssetComment GORM finders'
			AssetComment.count() == assetCommentCount + 2
			AssetComment.get(task.id).taskNumber == task.taskNumber
			AssetComment.findByProjectAndTaskNumber(project, 1).taskNumber == task.taskNumber
			AssetComment.get(assetComment.id).comment == assetComment.comment
	}

	private Date hourInDay(String dateTime) {
		return dateTime ? formatter.parse(aDay + ' ' + dateTime) : null
	}
}
