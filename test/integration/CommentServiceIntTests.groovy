import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityType
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import spock.lang.Specification

class CommentServiceIntTests extends Specification {

    def project
    def attributeSet
    def moveEvent
    def moveBundle
    def commentService
    def projectHelper

    void setup() {
        projectHelper = new ProjectTestHelper()
    }

    void 'test can find comments by AssetEntity instance'() {

        setup:
        def project = projectHelper.getProject()

        def entityType = EavEntityType.findByDomainName('AssetEntity')
        def attributeSet = new EavAttributeSet(attributeSetName: 'Server', entityType: entityType, sortOrder: 10).save()
        def moveEvent = new MoveEvent(name: "Example 1", project: project, inProgress: 'false').save()
        def moveBundle = new MoveBundle(name: 'Example 1', moveEvent: moveEvent, project: project, workflowCode: 'STD_PROCESS').save()
        def assetEntity = new AssetEntity(
                assetName: "Asset Name",
                assetType: "Asset Type",
                assetTag: 'TAG-0',
                moveBundle: moveBundle,
                project: project,
                attributeSet: attributeSet
        ).save()

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

}
