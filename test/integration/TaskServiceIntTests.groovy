import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.common.exceptions.ServiceException
import net.transitionmanager.service.InvalidParamException
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.test.spock.IntegrationSpec
import groovyx.gpars.GParsPool
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.PartyRole
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TaskService
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.SessionFactory
import spock.lang.Ignore
import spock.lang.See
import test.helper.ApiCatalogTestHelper

import java.util.concurrent.Future

class TaskServiceIntTests extends IntegrationSpec {

    CustomDomainService customDomainService
    TaskService taskService

    ApiCatalogTestHelper apiCatalogTestHelper
    AssetTestHelper assetTestHelper
    PersonTestHelper personTestHelper
    ProjectTestHelper projectTestHelper
    ProviderTestHelper providerTestHelper
    MoveBundleTestHelper moveBundleTestHelper

    SessionFactory sessionFactory
    GrailsApplication grailsApplication

	private Person adminPerson
	private Person untrustedPerson
    private UserLogin adminUser
    private UserLogin untrustedUser

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

    /**
     * Used to create the persons and users needed for some of the tests
     */
    private void createPersonAndUsers() {
        Project project = projectTestHelper.createProject()
		adminPerson = personTestHelper.createStaff(project.owner)
		untrustedPerson = personTestHelper.createStaff(project.owner)

        adminUser = personTestHelper.createUserLoginWithRoles(adminPerson, ["${SecurityRole.ADMIN}"])
        untrustedUser = personTestHelper.createUserLoginWithRoles(untrustedPerson, ["${SecurityRole.USER}"])
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

    // @Ignore
    void 'invocation of an api action show return status as started'() {
        setup: 'giving a local api action'
            Person whom = taskService.getAutomaticPerson()
            Project project = projectTestHelper.getProject()
            createPersonAndUsers()
            Provider provider = providerTestHelper.createProvider(project)
            ApiCatalog apiCatalog = apiCatalogTestHelper.createApiCatalog(project, provider)

            ApiAction apiAction = new ApiAction(
                project: project, provider: provider, name: RandomStringUtils.randomAlphanumeric(10),
                description: RandomStringUtils.randomAlphanumeric(10),
                apiCatalog: apiCatalog,
                connectorMethod: 'callEndpoint',
                methodParams: '[]',
                reactionScripts: '{"SUCCESS": "task.done()","STATUS": "if (response.status == SC.OK) {  return SUCCESS } else { return ERROR }","ERROR": "task.error( response.error )"}',
                reactionScriptsValid: 1,
                callbackMode: null,
                endpointUrl: 'http://www.yahoo.com', endpointPath: '/', isRemote:false)
            .save(failOnError: true)
        and: 'a test setup to automatically execute'
            AssetComment task = new AssetComment(
                    project: project,
                    comment: RandomStringUtils.randomAlphanumeric(10),
                    commentType: AssetCommentType.TASK,
                    apiAction: apiAction,
                    status: AssetCommentStatus.READY
            ).save(failOnError: true)
            sessionFactory.getCurrentSession().flush()

        when: 'the action is invoked without proper permission'
            task = taskService.invokeLocalAction(project, task.id, untrustedPerson)
        then: 'an exception should be thrown due to permissions'
            InvalidParamException ex = thrown()
            ex.message == 'You do not have permission to invoke the action'

        when: 'invoking the api action with an admin user'
            task = taskService.invokeLocalAction(project, task.id, adminPerson)
        then: 'an exception should be thrown because the task status is not correct'
            ex = thrown()
            ex.message == 'The task must be in the Ready or Started state in order to invoke action'

        when: 'the task status is READY'
            task.status = AssetCommentStatus.READY
        and: 'invoking the api action'
            task = taskService.invokeLocalAction(project, task.id, adminPerson)
        then: 'then the task should returned'
            null != task
        and: 'the task status should be Started'
            AssetCommentStatus.COMPLETED == task.status
    }

    @Ignore
    void 'simultaneous invocations of an api action should throw error'() {
        setup: 'giving an api action'
            Person whom = taskService.getAutomaticPerson()
            Project project = projectTestHelper.getProject()
            Provider provider = providerTestHelper.createProvider(project)
            ApiCatalog apiCatalog = apiCatalogTestHelper.createApiCatalog(project, provider)

            ApiAction apiAction = new ApiAction(project: project, provider: provider, name: RandomStringUtils.randomAlphanumeric(10),
                description: RandomStringUtils.randomAlphanumeric(10),
                apiCatalog: apiCatalog,
                connectorMethod: 'executeCall',
                methodParams: '[]',
                reactionScripts: '{"SUCCESS": "task.done()","STATUS": "return SUCCESS","ERROR": "task.error( response.error )", "DEFAULT": "task.error( response.error )"}',
                reactionScriptsValid: 1,
                callbackMode: null,
                endpointUrl: 'http://www.google.com',
                endpointPath: '/')
            .save(failOnError: true)

            AssetComment task = new AssetComment(
                    project: project,
                    taskNumber: RandomStringUtils.randomNumeric(5) as Integer,
                    comment: RandomStringUtils.randomAlphanumeric(10),
                    commentType: AssetCommentType.TASK,
                    apiAction: apiAction,
                    status: AssetCommentStatus.READY
            ).save(failOnError: true)
            sessionFactory.getCurrentSession().flush()

        when: 'simulating invoking the api action at approximately the same time'
            def promises = []
            GParsPool.withPool(2) {
                promises << taskService.&invokeLocalAction.callAsync(project, task.id, whom)
                promises << taskService.&invokeLocalAction.callAsync(project, task.id, whom)
            }

        then: 'transaction is rolled back and task status should show as ready for both invocations'
            def results = []
            promises.each { Future future ->
                if (future) {
                    results << future.get()
                }
            }

            AssetCommentStatus.READY == result[0]?.status
            AssetCommentStatus.READY == result[1]?.status
    }

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


    void 'addWhereConditionsForFieldSpecs'() {
        setup: 'create a project and load the fieldspecs'
            Project project = Project.read(Project.DEFAULT_PROJECT_ID)
            List<Map> fieldSpecs = customDomainService.fieldSpecs(project, AssetClass.DEVICE.toString(), CustomDomainService.ALL_FIELDS, ['field', 'label'])
            Map whereParams = [:]
            String whereSql = ''
            Map filter = [
			    class: 'device',
                asset: [
                    os: '%win%'
                ]
		    ]

        when: 'calling addWhereConditionsForFieldSpecs for a field name'
            whereSql = taskService.addWhereConditionsForFieldSpecs(fieldSpecs, filter, whereSql, whereParams)
        then: 'a LIKE statement should be generated'
            'a.os LIKE :os' == whereSql
        and: 'the OS query parameter should be added'
            whereParams.containsKey('os')
            whereParams.get('os') == '%win%'


        when: 'filter has the asset virtual criteria'
            filter.asset.put('virtual', true)
            whereSql = ''
            whereParams = [:]
        and: 'calling with no previous where'
            whereSql = taskService.addWhereConditionsForFieldSpecs(fieldSpecs, filter, whereSql, whereParams)
        then: 'the query will not have virtual'
            'a.os LIKE :os' == whereSql
        and: 'the os parameter was added'
            whereParams.containsKey('os')
            whereParams.get('os') == '%win%'

        when: 'filter has the field and label based criteria'
            filter.asset = [
                os: '%win%',
                Name: '%prod'
            ]
            whereSql = ''
            whereParams = [:]
        and: 'calling with no previous where'
            whereSql = taskService.addWhereConditionsForFieldSpecs(fieldSpecs, filter, whereSql, whereParams)
        then: 'the query should have both criteria'
            'a.assetName LIKE :assetName and a.os LIKE :os' == whereSql
        and: 'the parameters should have both parameters added'
            whereParams.containsKey('os')
            whereParams.get('os') == '%win%'
            whereParams.containsKey('assetName')
            whereParams.get('assetName') == '%prod'

        when: 'a filter is applied after other where logic was performed'
            filter.asset = [
                Description: 'abc'
            ]
        and: 'addWhereConditionsForFieldSpecs is called'
            whereSql = taskService.addWhereConditionsForFieldSpecs(fieldSpecs, filter, whereSql, whereParams)
        then: 'the where statement should have the Description criteria added'
            'a.assetName LIKE :assetName and a.os LIKE :os and a.description = :description' == whereSql
        and: 'the whereParams map has the 3rd parameter'
            whereParams.get('os') == '%win%'
            whereParams.get('assetName') == '%prod'
            whereParams.get('description') == 'abc'

    }

}
