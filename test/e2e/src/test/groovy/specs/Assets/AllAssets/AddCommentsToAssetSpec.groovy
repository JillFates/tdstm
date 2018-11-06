package specs.Assets.AllAssets

/**
 * This Spec is to verify comments added to an asset
 * @author Sebastian Bigatton
 */

import pages.Assets.AssetViews.ViewPage
import pages.Assets.AssetViews.CreateCommentPage
import pages.Assets.AssetViews.CommentDetailsPage
import pages.Assets.AssetViews.AssetDetailsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import geb.spock.GebReportingSpec
import spock.lang.Shared
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class AddCommentsToAssetSpec extends GebReportingSpec {

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static comment = baseName + " " + randStr + " comment"
    @Shared
    def category
    @Shared
    def beforeCommentsCount

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        assetsModule.goToAllAssets()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user opens comment pop up for an QAE2E asset already created in All Assets page"() {
        given: 'The User is in All Assets page'
            at ViewPage
        and: 'The user filters by QAE2E'
            filterByName baseName
        and: 'The User clicks on Asset'
            openRandomAssetDisplayed()
        and: 'Asset details modal is displayed'
            at AssetDetailsPage
            beforeCommentsCount = getCommentsCount()
        when: 'The user clicks on add comment button'
            clickOnAddComments()
        then: 'Create Comment pop up is displayed'
            at CreateCommentPage
    }

    def "2. The user cancels comment creation"(){
        when: 'The user clicks on cancel button'
            clickOnCancelButton()
        then: 'Asset details page is displayed'
            at AssetDetailsPage
    }

    def "3. The user adds a comment"(){
        when: 'The user clicks on add comment button'
            clickOnAddComments()
        then: 'Create Comment pop up is displayed'
            at CreateCommentPage
        when: 'The user sets comment'
            addComments comment
        then: 'Save button is enabled'
            verifySaveButtonIsDisplayed(true)
        when: 'The user selects random category'
            category = selectRandomCategory()
        and: 'The user clicks on save button'
            clickOnSaveButton()
        then: 'Asset details page is displayed'
            at AssetDetailsPage
    }

    def "4. The user certifies that the Comment was properly displayed in Asset details"(){
        when: 'The user is in Asset details page'
            at AssetDetailsPage
        then: 'The comment count was incremented'
            verifyCommentsCount(beforeCommentsCount)
        and: 'The comment is the same was saved'
            verifyAddedCommentText(comment, beforeCommentsCount)
        and: 'The category is the same was saved'
            verifyAddedCommentCategory(category, beforeCommentsCount)
    }

    def "5. The user certifies that the Comment was properly displayed in comment details"(){
        when: 'The user clicks on comment'
            clickOnAddedComment(beforeCommentsCount)
        then: 'Comment Detail pop up is displayed'
            at CommentDetailsPage
        and: 'The comment is the same was saved'
            verifyAddedCommentText(comment)
        and: 'The category is the same was saved'
            verifyAddedCommentCategory(category)
        and: 'The Archive checkbox is unchecked'
            verifyCheckedArchiveStatus(false)
        when: 'The user clicks on Cancel button'
            clickOnCancelButton()
        then: 'Asset details page is displayed'
            at AssetDetailsPage
    }
}