import net.transitionmanager.task.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentType
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.task.Task
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Specification
import test.helper.AssetCommentTestHelper
import test.helper.MoveEventTestHelper

@Integration
@Rollback
class CommentServiceIntTests extends Specification {

    AssetCommentTestHelper assetCommentTestHelper
    MoveEventTestHelper moveEventTestHelper

    def project
    def attributeSet
    def moveEvent
    def moveBundle
    def commentService
    def projectHelper
    def assetHelper
    void setup() {
        projectHelper = new ProjectTestHelper()
        assetHelper = new AssetTestHelper()
        assetCommentTestHelper = new AssetCommentTestHelper()
        moveEventTestHelper = new MoveEventTestHelper()
    }

    void 'test can find comments by AssetEntity instance'() {

        setup:
        def project = projectHelper.getProject()

        def assetEntity = assetHelper.createDevice(project, 'Server', [name: RandomStringUtils.randomAlphabetic(10), description: 'Red'])

        def task = new AssetComment(
                project: project,
                comment: "Sample for " + assetEntity.toString(),
                commentType: AssetCommentType.COMMENT,
                assetEntity: assetEntity
        ).save()

        when:
        def tasks = commentService.findAllByAssetEntity(assetEntity)

        then:
        [task] == tasks
    }


    void 'test filterTasks under different scenarios'() {
        given: 'a project with some tasks for different events'
            Project project = projectHelper.createProject()
            MoveEvent event1 = moveEventTestHelper.createMoveEvent(project)
            MoveEvent event2 = moveEventTestHelper.createMoveEvent(project)
            MoveEvent event3 = moveEventTestHelper.createMoveEvent(project)
            Task task1 = assetCommentTestHelper.createAssetComment(project, event1, false)
            Task task2 = assetCommentTestHelper.createAssetComment(project, event1)
            Task task3 = assetCommentTestHelper.createAssetComment(project, event1)
            Task task4 = assetCommentTestHelper.createAssetComment(project, event2)
        when: 'requesting published and unpublished tasks for the first event sorted by their comment.'
            Map params = ['event': event1.name, 'viewUnpublished': true]
            Map result = commentService.filterTasks(project, params, 'comment', 'desc', 10, 0)
        then: 'the total number of tasks is three'
            result['totalCount'] == 3
        and: 'the list of filtered tasks has three elements'
            result['tasks'].size == 3
        and: 'the sorting is correct'
            result['tasks'][0].comment.toUpperCase() > result['tasks'][1].comment.toUpperCase()
            result['tasks'][1].comment.toUpperCase() > result['tasks'][2].comment.toUpperCase()
        when: 'requesting multiple tasks (published only)'
            params['viewUnpublished'] = false
            result = commentService.filterTasks(project, params, 'comment', 'desc', 10, 0)
        then: 'two tasks were found'
            result['tasks'].size() == 2
        and: 'results are properly sorted'
            result['tasks'][0].comment.toUpperCase() > result['tasks'][1].comment.toUpperCase()
        when: 'requesting tasks for an event with no tasks'
            params = ['moveEvent': event3.id]
            result = commentService.filterTasks(project, params, 'comment', 'desc', 10, 0)
        then: 'no results were found'
            result['tasks'].size() == 0

    }

}
