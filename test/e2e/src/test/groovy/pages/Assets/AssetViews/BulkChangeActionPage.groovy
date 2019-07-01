package pages.Assets.AssetViews

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
        nextButton { bulkChangeActionModal.find("tds-button-custom.btn-primary.pull-left")}
        cancelButton { bulkChangeActionModal.find("button.tds-button-cancel")}
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

    def clickOnEditRadioButton(){
        editCheckbox.click()
    }

    def getActionMessageText(){
        actionMessage.text().trim()
    }

    def verifyActionMessageText(text){
        getActionMessageText() == text
    }

    def clickOnCancelButton(){
        waitFor{cancelButton.click()}
    }

    def clickOnNextButton(){
        nextButton.click()
    }
}