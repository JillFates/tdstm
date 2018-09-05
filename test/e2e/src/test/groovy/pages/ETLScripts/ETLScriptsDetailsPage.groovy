package pages.ETLScripts

import geb.Page
import modules.CommonsModule

class ETLScriptsDetailsPage extends Page{

    static at = {
        title == "ETL Scripts"
        datascriptDetail.text() == "ETL Script Detail"

    }

    static content = {
        modaltitle(required:false) { $("div", class:"modal-header").find("h4" , class:"modal-title")[0]}
        datascriptDetail { $('div.modal-header h4[_ngcontent-c7]')}
        dsDetailXIcon {$('div.modal.fade.in button.close')}
        datascriptEditBtn  { $("button", text: contains("Edit"))}
        dsProvider {$('.label-detail')[0]}
        dsName {$('.label-detail')[1]}
        dsMode {$('.label-detail')[2]}
        dsDescription {$('.label-detail')[3]}
        dsDesignerButton {$("data-script-view-edit").find("button", text: contains("ETL Script Designer"))}
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
