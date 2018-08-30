package pages.Assets

/**
 * @author Sebastian Bigatton
 */

import geb.Page
import modules.CommonsModule

class BulkChangeActionPage extends Page{
    static at = {
        bulkChangeActionModal.displayed
        modalTitle.text() == "Bulk Change Action"
        nextButton.displayed
        cancelButton.displayed
        closeButton.displayed
        editCheckboxLabel.text() == "Edit Assets"
        deleteCheckboxLabel.text() == "Delete Assets"
    }
    static content = {
        bulkChangeActionModal { $("#bulk-change-action-component")}
        modalTitle { bulkChangeActionModal.find(".modal-title")}
        nextButton { bulkChangeActionModal.find("button span.glyphicon-step-forward")}
        cancelButton { bulkChangeActionModal.find("button span.glyphicon-ban-circle")}
        closeButton { bulkChangeActionModal.find("button.close")}
        editCheckbox { bulkChangeActionModal.find("input", name: "operation")[0]}
        editCheckboxLabel { editCheckbox.parent()}
		deleteCheckbox { bulkChangeActionModal.find("input", name: "operation")[1]}
		deleteCheckboxLabel { deleteCheckbox.parent()}
        actionMessage { bulkChangeActionModal.find(".box-body .col-md-12").first().find(".form-group")}
        commonsModule { module CommonsModule}
    }

    def getEditRadioButtonStatus(){
        editCheckbox.jquery.prop('checked')
    }

    def clickOnDeleteRadioButton(){
        deleteCheckbox.click()
    }

    def getActionMessageText(){
        actionMessage.text().trim()
    }

    def clickOnCancelButton(){
        waitFor{cancelButton.click()}
    }

    def clickOnNextButton(){
        nextButton.click()
    }
}