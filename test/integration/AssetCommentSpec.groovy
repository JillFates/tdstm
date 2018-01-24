import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentType
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Specification


class AssetCommentSpec extends Specification {

    ApiActionTestHelper apiActionHelper
    ProjectTestHelper projectHelper

    void setup() {
        apiActionHelper = new ApiActionTestHelper()
        projectHelper = new ProjectTestHelper()
    }

    def "Test AssetComment consistency when changing actions"() {
        setup: "Create a project and a couple of actions"
            Project project1 = projectHelper.createProject()
            ApiAction action1 = apiActionHelper.createApiAction(project1)
            ApiAction action2 = apiActionHelper.createApiAction(project1)
            Date date = new Date()
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


}