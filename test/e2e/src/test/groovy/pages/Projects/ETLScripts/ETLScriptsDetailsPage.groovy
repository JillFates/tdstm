package pages.Projects.ETLScripts

import geb.Page
import modules.CommonsModule

class ETLScriptsDetailsPage extends Page{

    static at = {
        title == "ETL Scripts"
        datascriptDetail.text() == "ETL Script Detail"

    }

    static content = {
        detailsModal { $("div.data-script-view-edit-component")}
        datascriptDetail { detailsModal.find('div.modal-header h4[_ngcontent-c7]')}
        dsDetailXIcon { detailsModal.find('button.close')}
        datascriptEditBtn  { detailsModal.find("button", text: contains("Edit"))}
        dsProvider { detailsModal.find('.label-detail')[0]}
        dsName { detailsModal.find('.label-detail')[1]}
        dsMode { detailsModal.find('.label-detail')[2]}
        dsDescription { detailsModal.find('.label-detail')[3]}
        dsDesignerButton { detailsModal.find("button", text: contains("ETL Script Designer"))}
        commonsModule { module CommonsModule }
    }

    def clickOnDesignerButton(){
        waitFor{dsDesignerButton.click()}
    }

    def clickOnXButton(){
        waitFor{dsDetailXIcon.click()}
        commonsModule.waitForDialogModalHidden()
    }

    def clickOnEditButton(){
        waitFor{datascriptEditBtn.click()}
    }

    def getDSNameLabelText(){
        dsName.text()
    }

    def getDSDescriptionLabelText(){
        dsDescription.text()
    }

    def getDSModeLabelText(){
        dsMode.text()
    }

    def getDSProviderLabelText(){
        dsProvider.text()
    }

}
