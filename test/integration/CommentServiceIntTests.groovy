import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityType
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Specification

class CommentServiceIntTests extends Specification {

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

}
