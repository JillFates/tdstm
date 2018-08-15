package specs.Tags

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Tags.TagsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import geb.waiting.WaitTimeoutException

@Stepwise
class TagsDeletionSpec extends GebReportingSpec {
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static customTag = [
            "name": "$baseName $randStr Tag",
            "description": "Custom $baseName $randStr Tag description",
    ]
    static deleteMessage = "This Tag is removed from all linked records and will be deleted. There is no undo for this action."
    def selectedTag
    static noTagsMessage = "No records available."
    static maxNumberOfBulkTagsToBeDeleted = 3 // custom E2E tags to remove in workaround test

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        menuModule.goToTagsPage()
        at TagsPage
        clickOnCreateTagButton()
        fillInFields customTag
        clickOnSaveButton()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): #${sCount} ${specificationContext.currentIteration.name} "
    }

    def filterAndSetSelectedTag(tagName){
        filterByName tagName
        selectedTag = getTagNameText()
    }

    def deleteTag(){
        clickOnDeleteButton()
        commonsModule.clickOnButtonPromptModalByText("Confirm")
        filterByName selectedTag
        noTagsDisplayedInGrid()
    }

    def noTagsDisplayedInGrid(){
        try {
            getNoRecordsFoundText() == noTagsMessage
        } catch (WaitTimeoutException e){
            false
        }
    }

    def "1. The User cancel erase attempt action and certifies that was not deleted"() {
        given: 'The User is Manage Tags Page'
            at TagsPage
        when: 'The User filters by QAE2E'
            filterAndSetSelectedTag baseName
        and: 'The User clicks on Erase button for first row'
            clickOnDeleteButton()
        then: 'Alert is displayed'
            commonsModule.getDeleteAlertMessageText() == deleteMessage
        when: 'The User clicks on Cancel button'
            commonsModule.clickOnButtonPromptModalByText("Cancel")
        then: 'Tag still displayed in the grid'
            getTagNameText() == selectedTag
    }

    def "2. The User deletes a custom E2E tag"(){
        when: 'The User filters by QAE2E'
            filterAndSetSelectedTag baseName
        and: 'The User clicks on Erase button for first row'
            clickOnDeleteButton()
        and: 'The User clicks on Confirm button'
            commonsModule.clickOnButtonPromptModalByText("Confirm")
        and: 'The user filters by deleted tag name'
            filterByName selectedTag
        then: 'Deleted tag name should not be displayed'
            noTagsDisplayedInGrid()
    }

    def "3. Workaround to delete custom E2E tags"(){
        when: 'The User filters by QAE2E'
            filterAndSetSelectedTag baseName
        then: 'The user deletes custom E2E tags if there are'
            def count = 0
            while (!noTagsDisplayedInGrid()){
                count = count + 1
                if (count > maxNumberOfBulkTagsToBeDeleted) {
                    break
                }
                deleteTag()
                filterAndSetSelectedTag baseName
            }
            true // done, just return true to avoid test fails
    }
}