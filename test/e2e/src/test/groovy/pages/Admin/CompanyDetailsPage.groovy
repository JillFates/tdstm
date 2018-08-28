package pages.Admin

import geb.Page

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
        //nameField { $("td.name").find{it.text().contains("Name").next()}
        commentField { $("textarea[name=comment]")}
        partnerCheck { $("input[name=partner]")}
        //dateCreated { $("td.name").find{it.text().contains("Date Created")}.next()}
        //dateUpdated { $("td.name").find{it.text().contains("Date Updated")}.next()}
    }

    def getCompanyNameText(){
        labelColumn.find{it.text().contains("Name")}.next().text()
    }

    def getCompanyCommentText(){
        commentField.text()
    }

    def hasCompanyPartner(){
        partnerCheck.value()
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
}
