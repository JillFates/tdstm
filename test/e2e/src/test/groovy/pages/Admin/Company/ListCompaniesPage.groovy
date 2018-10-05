package pages.Admin.Company

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
        companyRowContainer (required: false, wait:true){ $("tr.ui-row-ltr")}
        nameField (required: false, wait:true){ companyRowContainer.find("td[aria-describedby=companyIdGrid_companyName] a")}
        partnerField (required: false, wait:true){ companyRowContainer.find("td[aria-describedby=companyIdGrid_partner]")}
        dateCreatedField (required: false, wait:true){ companyRowContainer.find("td[aria-describedby=companyIdGrid_dateCreated]")}
        dateUpdatedField(required: false, wait:true) { companyRowContainer.find("td[aria-describedby=companyIdGrid_lastUpdated]")}
        message (required: false, wait:true){$(class:"message")}
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

    def clickOnFirstElement(){
        nameField[0].click()
    }

    def validateMessage(text){
        message.text()==text
    }

    def validateNoResultsAreReturned(){
        companyRowContainer.size()==0
    }

    def validateCompanyIsListed(compName){
        nameField[0].text()==compName
    }
    /**
     * receives a boolean parameter. If the parameter is false, the expected text is "Yes"
     * since the value was changed.
     * @param value
     * @return
     */
    def validatePartnerField(value){
        if (value){
            partnerField.text()==" "
        }else{
            partnerField.text() =="Yes"
        }
    }
}
