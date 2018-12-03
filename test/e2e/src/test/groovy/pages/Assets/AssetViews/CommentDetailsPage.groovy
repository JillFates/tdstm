package pages.Assets.AssetViews

import geb.Page
import modules.CommonsModule

/**
 * This class represents the create comment modal
 * @author Sebastian Bigatton
 */
class CommentDetailsPage extends Page{
    static at = {
        commentsModal.displayed
        modalTitle.text().contains("Comment Detail")
        editButton.displayed
        cancelButton.displayed
        deleteButton.displayed
    }
    static content = {
        commentsModal { $("div#single-comment-component")}
        modalTitle { commentsModal.find(".modal-title")}
        editButton { commentsModal.find("button span.glyphicon-pencil")}
        cancelButton { commentsModal.find("button span.glyphicon-ban-circle")}
        deleteButton { commentsModal.find("button span.fa-trash")}
        comment { commentsModal.find("div.single-component div")}
        category { commentsModal.find("label", text: contains("Category:")).siblings("div").children()}
        archiveInput { commentsModal.find("#singleCommentArchive")}
        commonsModule { module CommonsModule}
    }

    def clickOnEditButton(){
        waitFor {editButton.click()}
    }

    def clickOnCancelButton(){
        waitFor {cancelButton.click()}
    }

    def verifyCheckedArchiveStatus(isChecked){
        archiveInput.jquery.prop('checked') == isChecked
    }

    def verifyAddedCommentText(commentText){
        comment.text() == commentText
    }

    def verifyAddedCommentCategory(categoryText){
        category.text() == categoryText
    }
}