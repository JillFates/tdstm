package pages.Admin.ImportAccounts

import geb.Page
import modules.AdminModule

class ImportAccountsPage extends Page{

    static at = {
        title == "Import Accounts"
        pageHeaderName.text() == "Import Accounts"
    }

    static content = {
        adminModule { module AdminModule}
        pageHeaderName { $("section", class:"content-header").find("h1")}
        step1Container { $("div", class:"account-import-step1")}
        importOptions { $("select#importOption option")}
        uploadButton { step1Container.find("button[type=submit]")}
        step2Container { $("div", class:"account-import-review")}
        step2Title { step2Container.find("h1")}
    }

    def randomSelectImportOption(){
        def randomOption = CommonActions.getSelectRandomOption importOptions
        randomOption.click()
    }

    def getImportAccuntsFilePath(){
        def classLoader = getClass().getClassLoader()
        def file = new File(classLoader.getResource("ImportAccounts-TM-Demo.xlsx").file)
        file.absolutePath
    }

    def selectsFileToImport(){
        $('form').importSpreadsheet = getImportAccuntsFilePath()
    }

    def clickUploadFile(){
        uploadButton.click()
    }

}
