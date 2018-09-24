package pages.Tasks.Cookbook

import geb.Page

class ErrorMessagePage extends Page {

    static at = {
        errorModalWindow.isDisplayed()
        errorModalTitle.text() == "Error"
        errorModalCloseBtn.text() == "Close"
    }

    static content = {
        errorModalWindow(wait:true)   { $("div#errorModal")}
        errorModalTitle(wait:true)    { $('div#errorModal').find("h4", class:"modal-title")}
        errorModalText                { $('div#errorModalText').find("li").text()}
        errorModalCloseBtn(wait:true) { $('div#errorModal').find("button", class: "btn btn-default")}
    }
}
