package pages.Admin

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

    // TODO: uncomment this method when dev/4.4.1 is merged to dev/4.5.0 branch
    /*
    static initializeCommonActions(){
        new CommonActions()
    }
    */

    // TODO: Remove this method when dev/4.4.1 is merged to dev/4.5.0 branch
    def getRandomOption(options) {
        def random = new Random()
        options.getAt(random.nextInt(options.size()))
    }

    def randomSelectImportOption(){
        // TODO: use commented line instead of line 38 when dev/4.4.1 is merged to dev/4.5.0 branch
        //def randomOption = initializeCommonActions().getSelectRandomOption userLoginsOptions
        def randomOption = getRandomOption importOptions
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
