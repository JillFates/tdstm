package pages.Assets.AssetViews

import geb.Page
import modules.CommonsModule
import utils.CommonActions

/**
 * This class represents the edit comment modal
 * @author Sebastian Bigatton
 */
class EditCommentPage extends Page{
    static at = {
        commentsModal.displayed
        modalTitle.text().contains("Edit Comment")
        saveButton.present
        deleteButton.displayed
        cancelButton.displayed
        textArea.displayed
    }
    static content = {
        commentsModal { $("div#single-comment-component")}
        modalTitle { commentsModal.find(".modal-title")}
        saveButton { commentsModal.find("button span.fa-floppy-o")}
        deleteButton { commentsModal.find("button span.fa-trash")}
        cancelButton { commentsModal.find("button span.glyphicon-ban-circle")}
        textArea { commentsModal.find("#singleComment")}
        categoryDropdownArrow { commentsModal.find("kendo-dropdownlist span.k-select")}
        categories { $("kendo-list li.k-item")}
        archiveCheckbox { commentsModal.find("#singleCommentArchive")}
        commonsModule { module CommonsModule}
    }

    def clickOnSaveButton(){
        waitFor {saveButton.click()}
        commonsModule.waitForLoader(5)
        commonsModule.waitForLoader(5)
    }

    def clickOnCancelButton(){
        waitFor {cancelButton.click()}
    }

    def addComments(comment){
        textArea = comment
        waitFor{saveButton.displayed}
    }

    def verifySaveButtonIsDisplayed(displayed = true){
        if (displayed){
            commonsModule.verifyElementDisplayed(saveButton)
        } else {
            !commonsModule.verifyElementDisplayed(saveButton)
        }
    }

    def selectRandomCategory(){
        categoryDropdownArrow.click()
        waitFor{categories[0].displayed}
        def option = CommonActions.getRandomOption categories
        def category = option.text()
        option.click()
        category
    }

    def clickOnArchive(){
        waitFor {archiveCheckbox.click()}
    }
}