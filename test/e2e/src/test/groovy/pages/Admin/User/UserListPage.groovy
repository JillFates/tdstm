package pages.Admin.User

import geb.Page
import modules.AdminModule
import modules.CommonsModule
import utils.CommonActions

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
        lockedIconFirstRow {$("tr", class:"ui-widget-content jqgrow ui-row-ltr")[0].find("img",src:"/tdstm/static/icons/lock_delete.png")}
    }

    def rowsDisplayed(){
        gridRows.displayed
    }

    def lockedIconDisplayed(){
        lockedIconFirstRow.displayed
    }

    def filterByUsername(username){
        waitFor{usernameFilter.displayed}
        usernameFilter = username
        commonsModule.waitForLoadingMessage()
    }

    /* Get common words to form full name to match with user name [baseName + random characters], but just because
    * both params could change, so take full name string from
    * base name + random char + common used for first name [firstNameAddition] - [firstNameAddition]
    * Result should be for example "QAE2Ea3S4"
    */
    def getRandomBaseUserNameByBaseName(baseName){
        def fullname = CommonActions.getRandomOption(fullnames).text()
        def firstNameAddition = "First"
        fullname.substring(0, baseName.length() + fullname.indexOf(firstNameAddition) - firstNameAddition.length())
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
    /**
     * validates the user displayed is the one expected
     */
    def isExpectedUser(userName, firstName, lastName){
        waitFor{$("td", "role": "gridcell", "aria-describedby": "userLoginIdGrid_username").find("a").text() == userName}
        waitFor{$("td", "role": "gridcell", "aria-describedby": "userLoginIdGrid_fullname").find("a").text().contains(firstName)}
        waitFor{$("td", "role": "gridcell", "aria-describedby": "userLoginIdGrid_fullname").find("a").text().contains(lastName)}
    }

    def isUserDeleted(){
        waitFor{message.text().contains "UserLogin not found"}
    }
}