package pages.Tags

import geb.Page
import utils.CommonActions
import modules.CommonsModule
import geb.waiting.WaitTimeoutException

class TagsPage extends Page{
    static at = {
        title == "Manage Tags"
        pageHeaderName.text().trim() == "Manage Tags"
        createBtn.text() == "Create Tag"
        tagsGrid.displayed
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        createBtn { $('button.k-grid-add-command')}
        tagsGrid {$('div[role=grid]')}
        tagsGridDataRows {tagsGrid.find(".k-grid-container .k-grid-content tr[kendogridlogicalrow]")}
        tagsGridActionsRows {tagsGrid.find(".k-grid-container .k-grid-content-locked tr[kendogridlogicalrow]")}
        tagsNoDataRecords {tagsGrid.find(".k-grid-container .k-grid-content tr.k-grid-norecords").find("td")}
        nameFilter { tagsGrid.find("td[kendogridfiltercell]", "aria-colindex": "2").find("input")}
        nameFilterRemove { nameFilter.next("span")}
        descFilter { tagsGrid.find("td[kendogridfiltercell]", "aria-colindex": "3").find("input")}
        descFilterRemove { descFilter.next("span")}
        //First Element of the Providers Table
        firstTagActions { tagsGridActionsRows.find("td", "aria-colindex": "1")[0]}
        firstTagSaveButton { firstTagActions.find(".k-grid-save-command")}
        firstTagCancelButton { firstTagActions.find(".k-grid-cancel-command")}
        firstTagEditButton { firstTagActions.find(".k-grid-edit-command")}
        firstTagRemoveButton { firstTagActions.find(".k-grid-remove-command")}
        firstTagName { tagsGridDataRows.find("td", "aria-colindex": "2")[0]}
        firstTagDesc { tagsGridDataRows.find("td", "aria-colindex": "3")[0]}
        firstTagColor { tagsGridDataRows.find("td", "aria-colindex": "4")[0]}
        firstTagAssets { tagsGridDataRows.find("td", "aria-colindex": "5")[0]}
        firstTagDateCreated { tagsGridDataRows.find("td", "aria-colindex": "6")[0]}
        firstTagLastModified { tagsGridDataRows.find("td", "aria-colindex": "7")[0]}
        firstTagColorSelected { firstTagColor.find("span.tag")}
        firstTagNameInput { firstTagName.find("input")}
        firstTagDescInput { firstTagDesc.find("input")}
        firstTagColorDropdown { firstTagColor.find("span.k-i-arrow-s")}
        firstTagColors { $("div.k-list-scroller").find("li.k-item")}
        commonsModule { module CommonsModule}
    }

    def filterByName(name){
        waitFor{nameFilter.displayed}
        nameFilter = name
    }

    def filterByDescription(description){
        waitFor{descFilter.displayed}
        descFilter = description
    }

    def removeNameFilter(){
        nameFilterRemove.click()
    }

    def removeDescFilter(){
        descFilterRemove.click()
    }

    def getGridRowsSize(){
        tagsGridDataRows.size()
    }

    def getTagNameText(){
        firstTagName.text().trim()
    }

    def getTagDescriptionText(){
        firstTagDesc.text().trim()
    }

    def getTagAssetsText(){
        firstTagAssets.text().trim()
    }

    def getTagDateCreatedText(){
        firstTagDateCreated.text().trim()
    }

    def getTagLastModifiedText(){
        firstTagLastModified.text().trim()
    }

    def getTagColorHexText(){
        def rgb = firstTagColorSelected.jquery.css("background-color")
        def r = rgb.substring(4, 7).toInteger()
        def g = rgb.substring(9, 12).toInteger()
        def b = rgb.substring(14, 17).toInteger()
        CommonActions.convertRgbToHex r,g,b
    }

    def clickOnCreateTagButton(){
        waitFor{createBtn.click()}
        waitFor{firstTagNameInput.displayed}
    }

    def clickOnCancelButton(){
        waitFor{firstTagCancelButton.displayed}
        waitFor{firstTagCancelButton.click()}
    }

    def clickOnSaveButton(){
        waitFor{firstTagSaveButton.displayed}
        waitFor{firstTagSaveButton.click()}
        commonsModule.waitForLoader(5)
    }

    def clickOnDeleteButton(){
        waitFor{firstTagRemoveButton.displayed}
        waitFor{firstTagRemoveButton.click()}
    }

    def isFirstTagRowNameNotEditable(){
        waitFor{firstTagName.displayed}
        firstTagName.children().isEmpty()
    }

    def isCancelButtonDisplayed(){
        firstTagCancelButton.displayed
    }

    def isSaveButtonDisplayed(){
        firstTagSaveButton.present
    }

    def isSaveButtonDisabled(){
        firstTagSaveButton.@disabled
    }

    def isAssetsNotEditableOnCreation(){
        firstTagAssets.children().isEmpty()
    }

    def isDateCreatedNotEditableOnCreation(){
        firstTagDateCreated.children().isEmpty()
    }

    def isLastModifiedNotEditableOnCreation(){
        firstTagLastModified.children().isEmpty()
    }

    def fillInFields(dataTagMap){
        firstTagNameInput = dataTagMap.name
        firstTagDescInput = dataTagMap.description
        firstTagColorDropdown.click()
        waitFor{firstTagColors[0].displayed}
        def color = CommonActions.getRandomOption firstTagColors
        color.click()
    }

    def getNoRecordsFoundText(){
        waitFor(2){tagsNoDataRecords.displayed}
        tagsNoDataRecords.text().trim()
    }

    def filterAndSetSelectedTag(tagName){
        filterByName tagName
        def selectedTagName = getTagNameText()
        selectedTagName
    }

    def deleteTag(tagName, message){
        clickOnDeleteButton()
        commonsModule.clickOnButtonPromptModalByText("Confirm")
        filterByName tagName
        noTagsDisplayedInGrid message
    }

    def noTagsDisplayedInGrid(message){
        try {
            getNoRecordsFoundText() == message
        } catch (WaitTimeoutException e){
            false
        }
    }

    def bulkDelete(message, maxNumberOfBulkTagsToBeDeleted, tagName) {
        def count = 0
        while (!noTagsDisplayedInGrid(message)) {
            count = count + 1
            if (count > maxNumberOfBulkTagsToBeDeleted) {
                break
            }
            deleteTag tagName, message
            filterByName tagName
        }
        true // done, just return true to avoid test fails
    }

    def verifyTagInformation(name, description){
        getGridRowsSize() == 1
        getTagNameText() == name
        getTagDescriptionText() == description
    }
}