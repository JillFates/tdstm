package pages.Projects.ETLScripts

import geb.Page
import modules.CommonsModule

class ETLScriptsDetailsPage extends Page{

    static at = {
        title == "ETL Scripts"
        datascriptDetail.text() == "ETL Script Detail"

    }

    static content = {
        detailsModal { $("div.modal-dialog.resize-disabled")}
        datascriptDetail { detailsModal.find("h3")[0]}
        dsDetailXIcon { $('clr-icon[shape="close"]').closest("button")[0]}
        datascriptEditBtn  { $('clr-icon[shape="pencil"]').closest("button")[0]}
        dsProvider { detailsModal.find('.label-detail')[0]}
        dsName { detailsModal.find('.label-detail')[1]}
        dsDescription { detailsModal.find('.label-detail')[2]}
        dsDesignerButton { $('clr-icon[shape="code"]').closest("button")[0]}
        commonsModule { module CommonsModule }
    }

    def clickOnDesignerButton(){
        waitFor{dsDesignerButton.click()}
    }

    def clickOnXButton(){
        waitFor{dsDetailXIcon.click()}
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
