package specs.Assets.AllAssets

/**
 * This Spec is to verify comments added to an asset
 * @author Sebastian Bigatton
 */

import pages.Assets.AssetViews.ViewPage
import pages.Assets.AssetViews.CreateCommentPage
import pages.Assets.AssetViews.EditCommentPage
import pages.Assets.AssetViews.CommentDetailsPage
import pages.Assets.AssetViews.AssetCreatePage
import pages.Assets.AssetViews.AssetDetailsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import geb.spock.GebReportingSpec
import spock.lang.Shared
import spock.lang.Stepwise
import utils.CommonActions

// import geb.driver.CachingDriverFactory

@Stepwise
class EditCommentsToAssetSpec extends GebReportingSpec {

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static assetName = "App ${randStr} with comment to edit without archive"
    static comment = "Comment ${randStr} to edit without archive"
    static editedComment = comment + " Edited"
    @Shared
    def category
    @Shared
    def beforeCommentsCount

    static appDataMap = [
            appName: assetName,
            appDesc: "App Description",
            appBundle: "Buildout",
            appStatus: "Confirmed"
    ]

    def setupSpec() {
        // CachingDriverFactory.clearCacheAndQuitDriver()

        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { assetsModule.goToApplications() }
        at ViewPage
        commonsModule.waitForLoader 5
        clickOnCreateButton()
        at AssetCreatePage
        createApplication appDataMap
        at AssetDetailsPage
        clickOnAddComments()
        at CreateCommentPage
        addComments comment
        selectRandomCategory()
        clickOnSaveButton()
        at AssetDetailsPage
        clickOnCloseButton()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user opens comment pop up for an asset already created in All Assets page"() {
        given: 'The User is in All Assets page'
            at ViewPage
        and: 'The user filters asset name and opens it'
            openAssetByName assetName
        and: 'Asset details modal is displayed'
            at AssetDetailsPage
            beforeCommentsCount = getCommentsCount()
        when: 'The user clicks on comment to be edited'
            clickOnAddedComment(0) // index, first and only comment in list
        then: 'Create Comment pop up is displayed'
            at CommentDetailsPage
        when: 'The user clicks on edit button'
            clickOnEditButton()
        then: 'Create Comment pop up is displayed'
            at EditCommentPage
    }

    def "2. The user cancels comment edition"(){
        when: 'The user clicks on cancel button'
            clickOnCancelButton()
        then: 'Asset details page is displayed'
            at AssetDetailsPage
    }

    def "3. The user edits the comment"(){
        when: 'The user clicks on comment to be edited'
            clickOnAddedComment(0) // index, first and only comment in list
        then: 'Create Comment pop up is displayed'
            at CommentDetailsPage
        when: 'The user clicks on edit button'
            clickOnEditButton()
        then: 'Create Comment pop up is displayed'
            at EditCommentPage
        when: 'The user sets comment'
            addComments editedComment
        and: 'The user selects random category'
            category = selectRandomCategory()
        and: 'The user clicks on save button'
            clickOnSaveButton()
        then: 'Asset details page is displayed'
            at AssetDetailsPage
    }

    def "4. The user certifies that the Comment is properly displayed in Asset details"(){
        when: 'The user is in Asset details page'
            at AssetDetailsPage
        then: 'The comment count was not incremented'
            verifyCommentsCount(beforeCommentsCount)
        and: 'The comment is the same was saved'
            verifyAddedCommentText(editedComment, 0) // index, first and only comment in list
        and: 'The category is the same was saved'
            verifyAddedCommentCategory(category, 0) // index, first and only comment in list
    }
}
