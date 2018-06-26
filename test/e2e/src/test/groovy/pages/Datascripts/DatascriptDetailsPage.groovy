package pages.Datascripts

import geb.Page

class DatascriptDetailsPage extends Page{

    static at = {
        title == "ETL Scripts"
        datascriptDetail.text() == "ETL Script Detail"

    }

    static content = {
        modaltitle(required:false) { $("div", class:"modal-header").find("h4" , class:"modal-title")[0]}
        datascriptDetail { $('div.modal-header h4[_ngcontent-c7]')}
        dsDetailXIcon {$('div.modal.fade.in button.close')}
        dsDesignerButton {$("data-script-view-edit").find("button", text: contains("ETL Script Designer"))}
        datascriptEditBtn  { $("button", text: contains("Edit"))}
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

}
