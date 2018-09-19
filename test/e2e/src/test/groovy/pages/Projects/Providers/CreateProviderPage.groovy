package pages.Projects.Providers

import geb.Page

class CreateProviderPage extends Page{

    static at = {
        title == "Providers"
        modaltitle.text() == "Create Provider"
        providerName.text() == "Name:*"
        providerDesc.text() == "Description:"
        providerComment.text() == "Comment:"

    }

    static content = {
        modaltitle(required:false) { $("div", class:"modal-header").find("h4" , class:"modal-title")[0]}
        providerName { $("label", for:"providerName")}
        providerDesc { $("label", for:"providerDescription")}
        providerComment { $("label", for:"providerComment")}
        providerNameField { $('input#providerName')}
        providerDescField { $('input#providerDescription')}
        providerCommentField { $('textarea#providerComment')}
        provSaveBtn { $("button", class:"btn btn-primary pull-left", type:"button")}
    }

}
