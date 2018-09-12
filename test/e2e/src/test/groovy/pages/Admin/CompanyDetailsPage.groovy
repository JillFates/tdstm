package pages.Admin

import geb.Page
import modules.CommonsModule

class CompanyDetailsPage extends Page{

    static at = {
        title == "Company"
        pageHeaderName.text() == "Admin Company"
        editButton.displayed
        deleteButton.displayed
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        editButton { $("input[value=Edit]")}
        deleteButton { $("input[value=Delete]")}
        message { $("div.message")}
        labelColumn { $("td.name")}
        commentField { $("textarea[name=comment]")}
        partnerCheck { $("input[name=partner]")}
        commonsModule { module CommonsModule }
        name {$('td.value')[0]}
    }

    def getCompanyNameText(){
        labelColumn.find{it.text().contains("Name")}.next().text()
    }

    def getCompanyCommentText(){
        commentField.text()
    }

    def hasCompanyPartner(){
        partnerCheck.jquery.prop('checked')
    }

    def getTextMessage(){
        message.text()
    }

    def getDateCreatedText(){
        labelColumn.find{it.text().contains("Date Created")}.next().text()
    }

    def getLastUpdatedText(){
        labelColumn.find{it.text().contains("Last Updated")}.next().text()
    }

    def clickDelete(){
        deleteButton.click()
    }

}
