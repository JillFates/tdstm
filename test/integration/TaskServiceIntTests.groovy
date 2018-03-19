import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.domain.ApiAction
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
import spock.lang.Specification

class TaskServiceIntTests extends IntegrationSpec {

    TaskService taskService
    AssetTestHelper assetTestHelper
    PersonTestHelper personTestHelper
    ProjectTestHelper projectTestHelper
    MoveBundleTestHelper moveBundleTestHelper
    GrailsApplication grailsApplication
    SessionFactory sessionFactory

    /* Test SetUp */
    void setup() {
        personTestHelper = new PersonTestHelper()
        projectTestHelper = new ProjectTestHelper()
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
                0 == task.isResolved

                // Test bumping status to COMPLETED after STARTED
                taskService.setTaskStatus(task, AssetCommentStatus.COMPLETED, whom)
                !task.actStart
                !task.actFinish
                !task.assignedTo
                !task.resolvedBy
                AssetCommentStatus.COMPLETED == task.status
                1 == task.isResolved
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
        EavEntityType entityType = new EavEntityType(entityTypeCode: RandomStringUtils.randomAlphabetic(10), domainName: RandomStringUtils.randomAlphabetic(10), isAuditable: 1).save(failOnError: true)
        EavAttributeSet attributeSet = new EavAttributeSet(attributeSetName: 'Server', entityType: entityType, sortOrder: 10).save(failOnError: true)
        MoveBundle moveBundle = moveBundleTestHelper.createBundle(project)

        // Create assets for Bundle
        AssetType.values().each {
            AssetEntity assetEntity = new AssetEntity(
                    assetName: it.toString(),
                    assetType: it.toString(),
                    assetTag: 'TAG-' + it.toString(),
                    moveBundle: moveBundle,
                    project: project,
                    attributeSet: attributeSet
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
            Provider provider = new Provider(project: project, name: RandomStringUtils.randomAlphanumeric(10)).save(failOnError: true)
            ApiAction apiAction = new ApiAction(project: project, provider: provider, name: RandomStringUtils.randomAlphanumeric(10),
            description: RandomStringUtils.randomAlphanumeric(10), agentClass: AgentClass.RESTFULL, agentMethod: 'executeCall',
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
            Provider provider = new Provider(project: project, name: RandomStringUtils.randomAlphanumeric(10)).save(failOnError: true)
            ApiAction apiAction = new ApiAction(project: project, provider: provider, name: RandomStringUtils.randomAlphanumeric(10),
            description: RandomStringUtils.randomAlphanumeric(10), agentClass: AgentClass.RESTFULL, agentMethod: 'executeCall',
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

        when: 'simulating invoking the api action at approximately the same time'
            def status1 = taskService.invokeAction(task, whom)
            def status2 = taskService.invokeAction(task, whom)

        then: 'first invocation should show as started and second should show as hold'
            null != status1
            null != status2
            'Started'   == status1
            'Hold'      == status2
    }
}
