package pages.Admin

import geb.Page

class CompanyCreationPage extends Page{

    static at = {
        title == "Create Company"
        pageHeaderName.text() == "Create Company"
        nameField.displayed
        saveButton.displayed
        cancelButton.displayed
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        saveButton { $("input.save")}
        cancelButton { $("input.cancel")}
        nameField { $("input#name")}
        commentField { $("textarea[name=comment]")}
        partnerCheck { $("input[name=partner]")}
    }

    def createCompany(companyData){
        nameField = companyData.name
        commentField = companyData.comment
        if (companyData.isPartner){
            partnerCheck.click()
        }
        saveButton.click()
    }
}
