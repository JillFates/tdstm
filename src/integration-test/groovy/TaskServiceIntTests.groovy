import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.core.GrailsApplication
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TaskService
import org.apache.commons.lang3.RandomStringUtils
import org.hibernate.SessionFactory
import spock.lang.Ignore
import spock.lang.See
import spock.lang.Specification
import test.helper.ApiCatalogTestHelper

@Integration
@Rollback
class TaskServiceIntTests extends Specification{

    TaskService taskService
    SecurityService securityService

    ApiCatalogTestHelper apiCatalogTestHelper
    AssetTestHelper assetTestHelper
    PersonTestHelper personTestHelper
    ProjectTestHelper projectTestHelper
    ProviderTestHelper providerTestHelper
    MoveBundleTestHelper moveBundleTestHelper

    SessionFactory    sessionFactory
    GrailsApplication grailsApplication

    /* Test SetUp */
    void setup() {
        personTestHelper = new PersonTestHelper()
        projectTestHelper = new ProjectTestHelper()
        providerTestHelper = new ProviderTestHelper()
        apiCatalogTestHelper = new ApiCatalogTestHelper()
        assetTestHelper = new AssetTestHelper()
        moveBundleTestHelper = new MoveBundleTestHelper()

        sessionFactory = grailsApplication.getMainContext().getBean('sessionFactory')
    }

    @Ignore
    void "test clean task data"() {
        setup:
            prepareMoveEventData()
            Person whom = personTestHelper.createPerson()

        when:
            def listAssetEntityNotNull = AssetComment.findAllByAssetEntityIsNotNull()

        then:
            listAssetEntityNotNull.each { task ->
                task = taskService.setTaskStatus(task, AssetCommentStatus.STARTED, whom)
                task.actStart != null
                task.assignedTo != null
                AssetCommentStatus.STARTED == task.status
                task.actFinish == null
                null == task.dateResolved

                // Test bumping status to COMPLETED after STARTED
                taskService.setTaskStatus(task, AssetCommentStatus.COMPLETED, whom)
                !task.actStart
                !task.actFinish
                !task.assignedTo
                !task.resolvedBy
                AssetCommentStatus.COMPLETED == task.status
                null != task.dateResolved
            }
    }

    private MoveEvent createMoveEvent(Project project) {
        MoveEvent moveEvent = new MoveEvent(
                name: "Example 1", project: project,
                inProgress: 'false').save(failOnError: true)
        return moveEvent
    }

    private List<AssetEntity> prepareMoveEventData() {
        Project project = projectTestHelper.getProject()
        MoveBundle moveBundle = moveBundleTestHelper.createBundle(project)

        // Create assets for Bundle
        AssetType.values().each {
            AssetEntity assetEntity = new AssetEntity(
                    assetName: it.toString(),
                    assetType: it.toString(),
                    assetTag: 'TAG-' + it.toString(),
                    moveBundle: moveBundle,
                    project: project
            )
            if (!assetEntity.validate() || !assetEntity.save(failOnError: true)) {
                String etext = "Unable to create assetEntity" +
                        GormUtil.allErrorsString(assetEntity)
                println etext
            }
        }

        // Create Tasks for AssetEntity
        AssetEntity.list()
    }

    void 'test can find tasks by AssetEntity instance'() {
        setup:
            Project project = projectTestHelper.getProject()

            AssetEntity assetEntity = assetTestHelper.createDevice(project, 'Server', [name: RandomStringUtils.randomAlphabetic(10), description: 'Red'])

            AssetComment task = new AssetComment(
                    project: project,
                    comment: "Sample for " + assetEntity.toString(),
                    commentType: AssetCommentType.TASK,
                    assetEntity: assetEntity
            ).save(failOnError: true)

        when:
            List<AssetComment> tasks = taskService.findAllByAssetEntity(assetEntity)

        then:
            [task] == tasks
    }

    void "test get cart quantities logging messages are written without error"() {
        setup:
            Project project = projectTestHelper.createProject()
            MoveEvent moveEvent = createMoveEvent(project)

        when:
            def cartQuantities = taskService.getCartQuantities(moveEvent, "test-cart")

        then:
            null != cartQuantities

    }

    void 'invocation of an api action show return status as started'() {
        setup: 'giving an api action'
            Person whom = taskService.getAutomaticPerson()
            Project project = projectTestHelper.getProject()
            Provider provider = providerTestHelper.createProvider(project)
            ApiCatalog apiCatalog = apiCatalogTestHelper.createApiCatalog(project, provider)

            ApiAction apiAction = new ApiAction(project: project, provider: provider, name: RandomStringUtils.randomAlphanumeric(10),
            description: RandomStringUtils.randomAlphanumeric(10), apiCatalog: apiCatalog, connectorMethod: 'executeCall',
            methodParams: '[]', reactionScripts: '{"SUCCESS": "success","STATUS": "status","ERROR": "error"}', reactionScriptsValid: 1,
            callbackMode: null, endpointUrl: 'http://www.google.com', endpointPath: '/').save(failOnError: true)

            AssetComment task = new AssetComment(
                    project: project,
                    comment: RandomStringUtils.randomAlphanumeric(10),
                    commentType: AssetCommentType.TASK,
                    apiAction: apiAction,
                    status: 'Ready'
            ).save(failOnError: true)
            sessionFactory.getCurrentSession().flush()

        when: 'invoking the api action'
            def status = taskService.invokeAction(task, whom)

        then: 'status should show task as started'
            null != status
            'Started'
    }

//    void 'simultaneous invocations of an api action should throw error'() {
//        setup: 'giving an api action'
//            Person whom = taskService.getAutomaticPerson()
//            Project project = projectTestHelper.getProject()
//            Provider provider = providerTestHelper.createProvider(project)
//            ApiCatalog apiCatalog = apiCatalogTestHelper.createApiCatalog(project, provider)
//
//            ApiAction apiAction = new ApiAction(project: project, provider: provider, name: RandomStringUtils.randomAlphanumeric(10),
//            description: RandomStringUtils.randomAlphanumeric(10), apiCatalog: apiCatalog, connectorMethod: 'executeCall',
//            methodParams: '[]', reactionScripts: '{"SUCCESS": "task.done()","STATUS": "return SUCCESS","ERROR": "task.error( response.error )", "DEFAULT": "task.error( response.error )"}', reactionScriptsValid: 1,
//            callbackMode: null, endpointUrl: 'http://www.google.com', endpointPath: '/').save(failOnError: true)
//
//            AssetComment task = new AssetComment(
//                    project: project,
//                    taskNumber: RandomStringUtils.randomNumeric(5) as Integer,
//                    comment: RandomStringUtils.randomAlphanumeric(10),
//                    commentType: AssetCommentType.TASK,
//                    apiAction: apiAction,
//                    status: 'Ready'
//            ).save(failOnError: true)
//            sessionFactory.getCurrentSession().flush()
//
//        when: 'simulating invoking the api action at approximately the same time'
//            def promises = []
//            GParsPool.withPool(2) {
//                promises << taskService.&invokeAction.callAsync(task, whom)
//                promises << taskService.&invokeAction.callAsync(task, whom)
//            }
//
//        then: 'transaction is rolled back and task status should show as ready for both invocations'
//            def results = []
//            promises.each { Future future ->
//                if (future) {
//                    results << future.get()
//                }
//            }
//            ['Ready', 'Ready'] == results
//    }

    @See('TM-12307')
    void 'test assign to me task'() {
        setup: 'given a task'
            Project project = projectTestHelper.getProject()
            Person whom = personTestHelper.createPerson()
            taskService.securityService = [
                    getUserCurrentProject: { return project },
                    getUserLoginPerson: { return whom },
                    getCurrentUsername: { return 'someone' }
            ] as SecurityService

            AssetComment task = new AssetComment(
                    project: project,
                    comment: RandomStringUtils.randomAlphanumeric(10),
                    commentType: AssetCommentType.TASK,
                    duration: 1,
                    durationScale: TimeScale.D,
                    status: AssetCommentStatus.PENDING
            ).save(failOnError: true)
            final assetCommentId1 = task.id

        when: 'assign task to me is call, task gets assigned to current logged in user'
            Person personAssignedTo = taskService.assignToMe(assetCommentId1, AssetCommentStatus.PENDING)

        then: 'current logged in user is returned as a result and the task assign to is updated'
            personAssignedTo
            personAssignedTo == whom
            task.assignedTo
            task.assignedTo == personAssignedTo

        when: 'assign to me is call but task status is not the expected'
            task = new AssetComment(
                    project: project,
                    comment: RandomStringUtils.randomAlphanumeric(10),
                    commentType: AssetCommentType.TASK,
                    duration: 1,
                    durationScale: TimeScale.D,
                    status: AssetCommentStatus.PENDING,
                    assignedTo: whom
            ).save(failOnError: true)
            final assetCommentId2 = task.id
            taskService.assignToMe(assetCommentId2, AssetCommentStatus.READY)
        then: 'exception is thrown'
            thrown(ServiceException)

        when: 'assign to me is call but task does not exists'
            taskService.assignToMe(-1, AssetCommentStatus.READY)

        then: 'Exception is thrown'
            thrown(EmptyResultException)
    }

    @See('TM-12307')
    void 'test change start and finish estimated time'() {
        setup: 'given a task'
            Project project = projectTestHelper.getProject()

            Date expectedStart = TimeUtil.nowGMT().plus(1)
            Date expectedFinish = expectedStart.plus(1)
            Date expectedStart2 = TimeUtil.nowGMT().plus(2)
            Date expectedFinish2 = expectedStart2.plus(1)

            AssetComment task = new AssetComment(
                    project: project,
                    comment: RandomStringUtils.randomAlphanumeric(10),
                    commentType: AssetCommentType.TASK,
                    duration: 1,
                    durationScale: TimeScale.D,
                    status: AssetCommentStatus.PENDING
            ).save(failOnError: true)
            final assetCommentId1 = task.id

        when: 'updating estimated start and finish time'
            AssetComment assetComment = taskService.changeEstTime(assetCommentId1, 1)

        then: 'task is estimated start and finish time get updated in db and asset comment is returned'
            assetComment
            TimeUtil.formatDate(assetComment.estStart) == TimeUtil.formatDate(expectedStart)
            TimeUtil.formatDate(assetComment.estFinish) == TimeUtil.formatDate(expectedFinish)

        when: 'having a task without duration and duration scale and change estimation time is called with 2 days'
            task = new AssetComment(
                    project: project,
                    comment: RandomStringUtils.randomAlphanumeric(10),
                    commentType: AssetCommentType.TASK,
                    status: AssetCommentStatus.PENDING
            ).save(failOnError: true)
            final assetCommentId2 = task.id
            assetComment = taskService.changeEstTime(assetCommentId2, 2)

        then: 'task duration and durationScale get updated to default values and task estimated start and finish times are set to 2 days ahead of current GMT time'
            assetComment
            assetComment.duration == 1
            assetComment.durationScale == TimeScale.D
            TimeUtil.formatDate(assetComment.estStart) == TimeUtil.formatDate(expectedStart2)
            TimeUtil.formatDate(assetComment.estFinish) == TimeUtil.formatDate(expectedFinish2)

        when: 'trying to update non existing task estimated start and finish times'
            taskService.changeEstTime(-1, 2)

        then: 'exception is thrown'
            thrown(EmptyResultException)
    }
}
