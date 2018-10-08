package pages.Projects.Providers

import geb.Page

class ProvidersEditionPage extends Page {
    static at = {
        title == "Providers"
        providerDetailHeader.text() == "Provider Edit"
        deleteBtn.text() == "Delete"
        saveBtn.text() == "Save"
    }

    static content = {
        providerDetailHeader(required: false, wait: true) { $('div.modal-header h4[_ngcontent-c7]') }
        provNameTxtField { $("input", id: "providerName", name: "providerName") }
        provDescTxtField { $("input", id: "providerDescription", name: "providerDescription") }
        provCommentTxtField { $("textarea", id: "providerComment", name: "providerComment") }
        saveBtn { $("button", class: "btn btn-primary pull-left", type: "button") }
        deleteBtn { $("button", class: "btn btn-danger") }
        closeXIcon { $("button", "aria-label": "Close", class: "close", type: "button").find("span", "aria-hidden": "true")[0] }
    }

}