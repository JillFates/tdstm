package pages.Admin.ExportAccounts

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

    def randomSelectUserLogins(){
        def randomOption = CommonActions.getSelectRandomOption userLoginsOptions
        randomOption.click()
    }

    def randomSelectStaffing(){
        def randomOption = CommonActions.getRandomOption staffingOptions
        randomOption.click()
    }

    def clickOnExportExcel(){
        exportButton.click()
    }
}
