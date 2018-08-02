import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification


class AssetCommentSpec extends Specification {

    @Shared
    ApiActionTestHelper apiActionHelper
    @Shared
    ProjectTestHelper projectHelper
    @Shared
    Project project1
    @Shared
    ApiAction action1
    @Shared
    ApiAction action2
    @Shared
    Date date = new Date()

    void setupSpec() {
        apiActionHelper = new ApiActionTestHelper()
        projectHelper = new ProjectTestHelper()

        //Create a project and a couple of actions
        project1 = projectHelper.createProject()
        action1 = apiActionHelper.createApiAction(project1)
        action2 = apiActionHelper.createApiAction(project1)
    }

    def "01. Test AssetComment consistency when changing actions"() {
        when: "Creating a new comment"
            AssetComment assetComment = new AssetComment()
            assetComment.with {
                comment = RandomStringUtils.randomAlphabetic(10)
                commentType = AssetCommentType.COMMENT
                apiAction = action1
                project = project1
                apiActionInvokedAt = date
                apiActionCompletedAt = date
            }
            assetComment.save(flush: true)
        then: "No Exception thrown"
            noExceptionThrown()
        and: "apiActionInvokedAt has the right value"
            assetComment.apiActionInvokedAt == date
        and: "apiActionCompletedAt has the right value"
            assetComment.apiActionCompletedAt == date
        when: "updating any value other than the action"
            assetComment.comment = RandomStringUtils.randomAlphabetic(10)
            assetComment.save(flush: true)
        then: "apiActionInvokedAt didn't change"
            assetComment.apiActionInvokedAt == date
        and: "apiActionCompletedAt didn't change"
            assetComment.apiActionCompletedAt == date
        when: "Switching actions"
            assetComment.apiAction = action2
            assetComment.save(flush: true)
        then: "the action was changed"
            assetComment.apiAction.id == action2.id
        and: "apiActionInvokedAt is null"
            assetComment.apiActionInvokedAt == null
        and: "apiActionCompletedAt is null"
            assetComment.apiActionCompletedAt == null
        when: "setting the action to null for a comment with not null apiActionInvokedAt and apiActionCompletedAt"
            assetComment.apiActionInvokedAt = date
            assetComment.apiActionCompletedAt = date
            assetComment.save(flush: true)
            assetComment.apiAction = null
            assetComment.save(flush: true)
        then: "apiActionInvokedAt is null"
            assetComment.apiActionInvokedAt == null
        and: "apiActionCompletedAt is null"
            assetComment.apiActionCompletedAt == null
    }

    def "02. Test isResolved() method"() {
        when: "Creating a new comment"
            AssetComment assetComment = new AssetComment()
            assetComment.with {
                comment = RandomStringUtils.randomAlphabetic(10)
                commentType = AssetCommentType.COMMENT
                apiAction = action1
                project = project1
                apiActionInvokedAt = date
                apiActionCompletedAt = date
            }
            Long id = assetComment.save(flush: true).id
        then: "the isResolved() method returns false"
            false == AssetComment.get(id).isResolved()
        and: "if a date resolved is set on the comment"
            assetComment.setDateResolved(TimeUtil.nowGMT())
        then: "the isResolved() method now returns true"
            true == AssetComment.get(id).isResolved()
    }
}