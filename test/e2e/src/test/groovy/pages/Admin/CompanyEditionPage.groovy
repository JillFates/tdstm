package pages.Admin

import geb.Page

/**
 * @author ingrid
 */
class CompanyEditionPage extends Page{

    static at = {
        title == "Edit Company"
        pageHeaderName.text() == "Edit Company"
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

    /**
     * Edits the company's name,comment and partner values.
     * @param partner
     * @return
     */
    def editCompany(partner){
        nameField=nameField.value()+" Edited"
        commentField=commentField.value()+" Edited"
        partnerCheck.click()
        saveButton.click()
    }
}
