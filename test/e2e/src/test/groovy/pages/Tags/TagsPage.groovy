package pages.Tags

import geb.Page
import utils.CommonActions
import modules.CommonsModule

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
        tagsGridRows {tagsGrid.find(".k-grid-container tr[kendogridlogicalrow]")}
        nameFilter { tagsGrid.find("td[kendogridfiltercell]", "aria-colindex": "2").find("input")}
        //First Element of the Providers Table
        firstTagActions { tagsGridRows.find("td", "aria-colindex": "1")[0]}
        firstTagSaveButton { firstTagActions.find(".k-grid-save-command")}
        firstTagCancelButton { firstTagActions.find(".k-grid-cancel-command")}
        firstTagEditButton { firstTagActions.find(".k-grid-edit-command")}
        firstTagRemoveButton { firstTagActions.find(".k-grid-remove-command")}
        firstTagName { tagsGridRows.find("td", "aria-colindex": "2")[0]}
        firstTagDesc { tagsGridRows.find("td", "aria-colindex": "3")[0]}
        firstTagColor { tagsGridRows.find("td", "aria-colindex": "4")[0]}
        firstTagAssets { tagsGridRows.find("td", "aria-colindex": "5")[0]}
        firstTagDateCreated { tagsGridRows.find("td", "aria-colindex": "6")[0]}
        firstTagLastModified { tagsGridRows.find("td", "aria-colindex": "7")[0]}
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

    def getGridRowsSize(){
        tagsGridRows.size()
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

    static commonActions = new CommonActions()

    def getTagColorHexText(){
        def rgb = firstTagColorSelected.jquery.css("background-color")
        def r = rgb.substring(4, 7).toInteger()
        def g = rgb.substring(9, 12).toInteger()
        def b = rgb.substring(14, 17).toInteger()
        commonActions.convertRgbToHex r,g,b
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
        def color = commonActions.getRandomOption firstTagColors
        color.click()
    }
}