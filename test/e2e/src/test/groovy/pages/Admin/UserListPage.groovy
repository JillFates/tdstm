package pages.Admin

import geb.Page
import modules.AdminModule
import modules.CommonsModule

class UserListPage extends Page{

    static at = {
        title == "User List - Active Users"
        pageHeaderName.text() == "UserLogin List - Active Users"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
        usernameFilter { $("#gs_username")}
        personFilter {$("#gs_fullname")}
        usernames { $("[aria-describedby=userLoginIdGrid_username] a")}
        fullnames { $("[aria-describedby=userLoginIdGrid_fullname] a")}
        userDeletedMessage { $(".message")}
        commonsModule { module CommonsModule }
        adminModule { module AdminModule}

        gridRows {$("#userLoginIdGrid").find("role":"row")}
        gridSize {gridRows.size()}
    }

    def rowsDisplayed(){
        gridRows.displayed
    }

    def filterByUsername(username){
        waitFor{usernameFilter.displayed}
        usernameFilter = username
        commonsModule.waitForLoadingMessage()
    }

    def getFirstUserLastNameDisplayed(){
        def fullname = fullnames.first().text()
        fullname.substring(fullname.lastIndexOf(" ") + 1)
    }

    def clickOnFirstUserName(){
        waitFor{usernames.first().click()}
    }

    def verifyDeletedMessage(){
        waitFor{userDeletedMessage.displayed}
        userDeletedMessage.text().contains("deleted")
    }

    def getGridRowsSize(){
        usernames.size()
    }
    /**
     * The filed is used to filter by First and/or Last name
     */
    def filterByPerson(person){
        personFilter = person
    }
}