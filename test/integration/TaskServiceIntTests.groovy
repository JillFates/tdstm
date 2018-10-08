import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.grails.GormUtil
import grails.test.spock.IntegrationSpec
import groovyx.gpars.GParsPool
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.service.TaskService
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.SessionFactory
import spock.lang.Ignore
import test.helper.ApiCatalogTestHelper
import test.helper.AssetCommentTestHelper
import test.helper.MoveEventTestHelper

import java.util.concurrent.Future

class TaskServiceIntTests extends IntegrationSpec {

    TaskService taskService

    ApiCatalogTestHelper apiCatalogTestHelper
    AssetTestHelper assetTestHelper
    PersonTestHelper personTestHelper
    ProjectTestHelper projectTestHelper
    ProviderTestHelper providerTestHelper
    MoveBundleTestHelper moveBundleTestHelper

    SessionFactory sessionFactory
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

    void 'simultaneous invocations of an api action should throw error'() {
        setup: 'giving an api action'
            Person whom = taskService.getAutomaticPerson()
            Project project = projectTestHelper.getProject()
            Provider provider = providerTestHelper.createProvider(project)
            ApiCatalog apiCatalog = apiCatalogTestHelper.createApiCatalog(project, provider)

            ApiAction apiAction = new ApiAction(project: project, provider: provider, name: RandomStringUtils.randomAlphanumeric(10),
            description: RandomStringUtils.randomAlphanumeric(10), apiCatalog: apiCatalog, connectorMethod: 'executeCall',
            methodParams: '[]', reactionScripts: '{"SUCCESS": "task.done()","STATUS": "return SUCCESS","ERROR": "task.error( response.error )", "DEFAULT": "task.error( response.error )"}', reactionScriptsValid: 1,
            callbackMode: null, endpointUrl: 'http://www.google.com', endpointPath: '/').save(failOnError: true)

            AssetComment task = new AssetComment(
                    project: project,
                    taskNumber: RandomStringUtils.randomNumeric(5) as Integer,
                    comment: RandomStringUtils.randomAlphanumeric(10),
                    commentType: AssetCommentType.TASK,
                    apiAction: apiAction,
                    status: 'Ready'
            ).save(failOnError: true)
            sessionFactory.getCurrentSession().flush()

        when: 'simulating invoking the api action at approximately the same time'
            def promises = []
            GParsPool.withPool(2) {
                promises << taskService.&invokeAction.callAsync(task, whom)
                promises << taskService.&invokeAction.callAsync(task, whom)
            }

        then: 'transaction is rolled back and task status should show as ready for both invocations'
            def results = []
            promises.each { Future future ->
                if (future) {
                    results << future.get()
                }
            }
            ['Ready', 'Ready'] == results
    }
}
