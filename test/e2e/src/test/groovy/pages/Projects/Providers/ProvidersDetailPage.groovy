package pages.Projects.Providers

import geb.Page
import modules.CommonsModule

class ProvidersDetailPage extends Page{
    static at = {
        title == "Providers"
        providerDetailHeader.text() == "Provider Detail"
        editBtn.text() == "Edit"
        deleteBtn.text() == "Delete"
    }

    static content = {
        providerDetailHeader(required:false , wait:true) { $('div.modal-header h4[_ngcontent-c7]')}
        providerName { $("div" ,  class:"col-sm-9").find("label" , class:"control-label label-detail")[0]}
        providerDesc { $("div" ,  class:"col-sm-9").find("label" , class:"control-label label-detail")[1]}
        editBtn {$("button", class:"btn btn-primary pull-left" , type:"button")}
        deleteBtn {$("button", class:"btn btn-danger")}
        closeXIcon {$("button", "aria-label":"Close" , class:"close" , type:"button").find("span", "aria-hidden":"true")[0]}
        commonsModule { module CommonsModule }
    }

    def clickDeleteButton(){
        waitFor{deleteBtn.click()}
    }

    def clickOnXButton(){
        waitFor{closeXIcon.click()}
        commonsModule.waitForDialogModalHidden()
    }

}
