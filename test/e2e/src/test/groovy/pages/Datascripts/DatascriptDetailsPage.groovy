package pages.Datascripts

import geb.Page
import modules.CommonsModule

class DatascriptDetailsPage extends Page{

    static at = {
        title == "ETL Scripts"
        datascriptDetail.text() == "ETL Script Detail"

    }

    static content = {
        modaltitle(required:false) { $("div", class:"modal-header").find("h4" , class:"modal-title")[0]}
        datascriptDetail { $('div.modal-header h4[_ngcontent-c7]')}
        dsDetailXIcon {$("div", class:"modal-header").find("button","aria-label":"Close", class:"close")[0]}
        dsDesignerButton {$("data-script-view-edit").find("button", text: contains("DataScript Designer"))}
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
        commonsModule.waitForEtlScriptsModalHidden()
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
