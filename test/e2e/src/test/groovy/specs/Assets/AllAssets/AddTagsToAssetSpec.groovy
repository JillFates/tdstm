package specs.Assets.AllAssets

/**
 * This Spec is to verify tags added to an asset
 * @author Sebastian Bigatton
 */

import pages.Assets.AssetViews.ViewPage
import pages.Assets.AssetViews.AssetEditPage
import pages.Assets.AssetViews.AssetDetailsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class AddTagsToAssetSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    static defaultTagNames = ["GDPR", "HIPPA", "PCI", "SOX"]
    static randomDefaultTag = CommonActions.getRandomOption defaultTagNames
    static finalTagsSelectedList = [randomDefaultTag]


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

    def "1. The user opens a QAE2E task already created in All Assets page"() {
        given: 'The User is in All Assets page'
            at ViewPage
        when: 'The user filters by QAE2E'
            filterByName baseName
        and: 'The User clicks on Asset'
            openFirstAssetDisplayed()
        then: 'Asset details modal is displayed'
            at AssetDetailsPage
    }

    def "2. The user randomly chooses and remove some tags"(){
        when: 'The user clicks on edit button'
            clickOnEditButton()
        then: 'Asset edit modal is displayed'
            at AssetEditPage
        when: 'The user cleans pre existing tags'
            removeAllTagsIfExists()
        and: 'The user adds some random tags'
            selectRandomTag(2)
        and: 'The user removes all again'
            removeAllTagsIfExists()
        then: 'No tags are selected'
            verifyNoTagsSelected()
    }

    def "3. The user randomly verifies 4 default tags"(){
        when: 'The user opens the dropdown'
            clickOnTagsDropDown()
        then: 'Asset edit modal is displayed'
            verifyDisplayedTagsByName defaultTagNames
    }

    def "4. The user adds tags and updates asset"(){
        when: 'The user selects a default tag'
            selectTagByName randomDefaultTag
        and: 'The user selects some random tags'
            def randomSelectedTags = selectRandomTag(2)
            randomSelectedTags.each{finalTagsSelectedList.add(it)}
        and: 'The user clicks on update button'
            clickOnSaveButton()
        then: 'Asset details modal is displayed'
            at AssetDetailsPage
        and: 'All selected tags are displayed'
            verifyTagNamesDisplayed finalTagsSelectedList
    }
}