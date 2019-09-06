package net.transitionmanager.task.timeline

import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import test.helper.ApplicationTestHelper
import test.helper.AssetCommentTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.MoveEventTestHelper
import test.helper.ProjectTestHelper

import java.text.SimpleDateFormat

import static com.tdsops.tm.enums.domain.AssetCommentStatus.PLANNED

@Integration
@Rollback
class TimeLineServiceIntegrationSpec extends Specification {

	@Shared
	ProjectTestHelper projectTestHelper

	@Shared
	MoveBundleTestHelper moveBundleTestHelper

	@Shared
	MoveEventTestHelper moveEventTestHelper

	@Shared
	AssetCommentTestHelper assetCommentTestHelper

	@Shared
	ApplicationTestHelper applicationTestHelper

	@Shared
	Project project

	@Shared
	MoveBundle moveBundle

	@Shared
	MoveEvent event

	@Shared
	SimpleDateFormat formatter = new SimpleDateFormat('MM/dd/yyyy hh:mm')

	@Shared
	String aDay = '06/22/2018'

	@Autowired
	TimeLineService timeLineService

	void setup() {
		projectTestHelper = new ProjectTestHelper()
		assetCommentTestHelper = new AssetCommentTestHelper()
		moveBundleTestHelper = new MoveBundleTestHelper()
		moveEventTestHelper = new MoveEventTestHelper()
		applicationTestHelper = new ApplicationTestHelper()

		project = projectTestHelper.createProject(null)
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		event = moveEventTestHelper.createMoveEvent(project)
	}

	/*
										 +-----+---+-----+
										 |06:30| B |07:10|
						   +------------>+-----+---+-----+
						   |             |06:30| 40|07:10|
						   |             +-----+---+-----+
						   |
		   +-----+---+-----|
		   |06:00| A |06:30|
		   +-----+---+-----+
		   |06:00| 30|06:30|
		   +-----+---+-----|
						   |
						   |       +-----+---+-----+
						   |       |06:30| A |06:50|
						   +------>+-----+---+-----+
								   |06:50| 20|07:10|
								   +-----+---+-----+
		   +-----------------------+---------------+--------+
		 06:00                   06:30           06:50    07:10
 */

	@Unroll
	void 'test can calculate critical path for a graph with three TaskVertex A status=#statusA, B status=#statusB and C status=#statusC, window end time=#endTime and current time=#current'() {

		setup: 'a TaskTimeLineGraph with a list of TaskVertex'
			event.estStartTime = hourInDay('06:00')
			event.estCompletionTime = hourInDay(endTime)
			event.save()

			Task taskA = new Task(project: project, taskNumber: 1, comment: 'A', duration: 30, actStart: hourInDay(startA), status: statusA, moveEvent: event).save()
			Task taskB = new Task(project: project, taskNumber: 2, comment: 'B', duration: 40, actStart: hourInDay(startB), status: statusB, moveEvent: event).save()
			Task taskC = new Task(project: project, taskNumber: 3, comment: 'C', duration: 20, actStart: hourInDay(startC), status: statusC, moveEvent: event).save()

			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS').save()
			TaskDependency edgeAC = new TaskDependency(id: 102, predecessor: taskA, assetComment: taskC, type: 'SS').save()

			TimelineSummary timelineSummary = timeLineService.executeCPA(event, [taskA, taskB, taskC], [edgeAB, edgeAC])

		expect:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [taskA.comment, taskB.comment]
			}

		where:
			endTime | current | startA | statusA | startB | statusB | startC | statusC || cpA  | slackA | esA     | efA     | lsA     | lfA     | cpB  | slackB | esB     | efB     | lsB     | lfB     | cpC   | slackC | esC     | efC     | lsC     | lfC
			'07:10' | '06:00' | null   | PLANNED | null   | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00' | '06:30' | true | 0      | '06:30' | '07:10' | '06:30' | '07:10' | false | 20     | '06:30' | '06:50' | '06:50' | '07:10'

	}

	private Date hourInDay(String dateTime) {
		return dateTime ? formatter.parse(aDay + ' ' + dateTime) : null
	}
}
