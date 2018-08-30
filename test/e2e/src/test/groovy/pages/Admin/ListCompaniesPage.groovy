package pages.Admin

import geb.Page
import modules.AdminModule
import modules.CommonsModule

class ListCompaniesPage extends Page{

    static at = {
        title == "Company List"
        pageHeaderName.text() == "Company List"
        createCompanyButton.displayed
        nameFilter.displayed
    }

    static content = {
        adminModule { module AdminModule}
        commonsModule { module CommonsModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
        createCompanyButton { $("input", value: "Create Company")}
        nameFilter { $("input#gs_companyName")}
        companyRowContainer { $("tr.ui-row-ltr")}
        nameField { companyRowContainer.find("td[aria-describedby=companyIdGrid_companyName] a")}
        partnerField { companyRowContainer.find("td[aria-describedby=companyIdGrid_partner]")}
        dateCreatedField { companyRowContainer.find("td[aria-describedby=companyIdGrid_dateCreated]")}
        dateUpdatedField { companyRowContainer.find("td[aria-describedby=companyIdGrid_lastUpdated]")}
    }

    def clickOnCreateButton(){
        waitFor{createCompanyButton.click()}
    }

    def filterByName(name){
        nameFilter = name
        commonsModule.waitForLoadingMessage()
    }

    def getCompanyNameText(){
        nameField.text()
    }

    def hasCompanyPartner(){
        partnerField.text()
    }

    def getDateCreatedText(){
        dateCreatedField.text()
    }

    def getLastUpdatedText(){
        dateUpdatedField.text()
    }
}
