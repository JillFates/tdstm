package pages.Assets.AssetViews

/**
 * This class represents the new generic asset edit modal
 * page
 * @author Sebastian Bigatton
 */
import geb.Page
import modules.CommonsModule
import utils.CommonActions
import geb.waiting.WaitTimeoutException

class AssetEditPage extends Page {

    static at = {
        assetDetailModal.displayed
        modalTitle.text().contains("Edit")
    }

    static content = {
        assetDetailModal { $("div.tds-angular-component-content") }
        modalTitle { assetDetailModal.find(".modal-title") }
        updateButton { assetDetailModal.find("button", text: contains("Update")) }
        cancelButton { assetDetailModal.find("button", text: contains("Delete")) }
        deleteButton { assetDetailModal.find("button", text: contains("Cancel")) }
        assetTagSelector { assetDetailModal.find("tds-asset-tag-selector") }
        tagsDropDown { assetTagSelector.find(".component-action-open") }
        tagsOptionList { $("div.k-list-container").find("li.k-item") }
        tagNamesSelector { tagsOptionList.find("div.asset-tag-selector-single-item") }
        deleteTagIcon { assetTagSelector.find("li span.k-i-close") }
        tagsInput { assetTagSelector.find("input#asset-tag-selector-component") }
        tagsSelected { assetTagSelector.find("kendo-taglist li") }
        commonsModule { module CommonsModule }
    }

    def clickOnUpdateButton() {
        waitFor { updateButton.click() }
    }

    def clickOnCloseButton() {
        waitFor { closeButton.click() }
    }

    def clickOnTagsInput() {
        tagsInput.click()
    }

    def clickOnTagsDropDown() {
        waitFor { tagsDropDown.present }
        js.'$("tds-asset-tag-selector .component-action-open").click()'
        waitFor{tagNamesSelector.size() > 1}
    }

    def selectTagByName(tagName) {
        clickOnTagsDropDown()
        def tag = tagNamesSelector.find { it.text().trim() == tagName }
        commonsModule.goToElement tag // to avoid stale element error, auto scroll and go to it
        tag.click()
    }

    def selectRandomTag(int numberOfTags) {
        clickOnTagsDropDown()
        def randomTags = CommonActions.getRandomOptions tagNamesSelector, numberOfTags
        def randomTagNames = []
        randomTags.each { // get tags text before close DD and lost references
            commonsModule.goToElement it
            randomTagNames.add it.text().trim()
        }
        clickOnTagsInput() // close DD for next action

        randomTagNames.each {
            selectTagByName it
        }
        randomTagNames
    }

    def verifyDisplayedTagsByName(tagNamesList) {
        def found = false
        tagNamesList.each { tagName ->
            if (tagNamesSelector.find { it.text().trim() == tagName }) {
                found = true
            }
            assert found, "$tagName was not found in the tags list"
        }
        clickOnTagsInput() // close DD for next action
        true // assertion is inside iteration, just prevent this break
    }

    def removeAllTagsIfExists() {
        try {
            // try to delete a list of tags
            waitFor(1) { deleteTagIcon.size() > 1 }
            deleteTagIcon.each {
                it.click()
            }
        } catch (WaitTimeoutException e) {
            // just one tag found delete it
            try {
                waitFor(1) { deleteTagIcon.displayed }
                deleteTagIcon.click()
            } catch (WaitTimeoutException e2) {
                // no tags selected
            }
        }
    }

    def verifyNoTagsSelected() {
        !commonsModule.verifyElementDisplayed(assetTagSelector.find("kendo-taglist li"))
    }
}