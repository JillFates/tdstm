import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Specification

class TaskServiceIntTests extends Specification {

    def taskService
    AssetTestHelper assetTestHelper
    PersonTestHelper personTestHelper
    ProjectTestHelper projectTestHelper
    MoveBundleTestHelper moveBundleTestHelper

    /* Test SetUp */
    void setup() {
        personTestHelper = new PersonTestHelper()
        projectTestHelper = new ProjectTestHelper()
        assetTestHelper = new AssetTestHelper()
        moveBundleTestHelper = new MoveBundleTestHelper()
    }

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

    private createMoveEvent(Project project) {
        MoveEvent moveEvent = new MoveEvent(
                name: "Example 1", project: project,
                inProgress: 'false').save(failOnError: true)
        return moveEvent
    }

    private prepareMoveEventData() {
        Project project = projectTestHelper.getProject()
        def entityType = new EavEntityType(entityTypeCode: RandomStringUtils.randomAlphabetic(10), domainName: RandomStringUtils.randomAlphabetic(10), isAuditable: 1).save(failOnError: true)
        def attributeSet = new EavAttributeSet(attributeSetName: 'Server', entityType: entityType, sortOrder: 10).save(failOnError: true)
        MoveBundle moveBundle = moveBundleTestHelper.createBundle(project)

        // Create assets for Bundle
        AssetType.values().each {
            def assetEntity = new AssetEntity(
                    assetName: it.toString(),
                    assetType: it.toString(),
                    assetTag: 'TAG-' + it.toString(),
                    moveBundle: moveBundle,
                    project: project,
                    attributeSet: attributeSet
            )
            if (!assetEntity.validate() || !assetEntity.save(failOnError: true)) {
                def etext = "Unable to create assetEntity" +
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

        def assetEntity = assetTestHelper.createDevice(project, 'Server', [name: RandomStringUtils.randomAlphabetic(10), description: 'Red'])

        def task = new AssetComment(
                project: project,
                comment: "Sample for " + assetEntity.toString(),
                commentType: AssetCommentType.TASK,
                assetEntity: assetEntity
        ).save(failOnError: true)

        when:
        def tasks = taskService.findAllByAssetEntity(assetEntity)

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

}
