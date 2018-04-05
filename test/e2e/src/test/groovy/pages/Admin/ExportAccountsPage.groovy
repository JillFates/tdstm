package pages.Admin

import geb.Page
import modules.AdminModule
import utils.CommonActions

class ExportAccountsPage extends Page {

    static at = {
        title == "Export Accounts"
        pageHeaderName.text() == "Export Accounts"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
        userLoginsOptions {$("select#loginChoice option")}
        staffingOptions {$("form div input[type=radio]")}
        exportButton {$("form button[type=submit]")}
    }

    static initializeCommonActions(){
        new CommonActions()
    }

    def randomSelectUserLogins(){
        def randomOption = initializeCommonActions().getSelectRandomOption userLoginsOptions
        randomOption.click()
    }

    def randomSelectStaffing(){
        def randomOption = initializeCommonActions().getRandomOption staffingOptions
        randomOption.click()
    }

    def clickOnExportExcel(){
        exportButton.click()
    }
}
