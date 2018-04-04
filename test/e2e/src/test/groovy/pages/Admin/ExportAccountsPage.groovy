package pages.Admin

import geb.Page
import modules.AdminModule
import utils.CommonActions
import groovy.io.FileType

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

    def waitForDownloadedFile(fileName){
        def count = 0
        def file = getAllFilesFromUserHomeDownloadsFolder()[0]
        while(!file.name.startsWith(fileName) && count < 5) {
            file = getAllFilesFromUserHomeDownloadsFolder()[0]
            Thread.sleep(1000)
            count++
        }
        assert file.name.startsWith(fileName), "File download has exceeded 5 seconds"
    }

    def getAllFilesFromUserHomeDownloadsFolder(){
        def home = System.getProperty("user.home")
        def directory = new File(home+"/Downloads/")
        def files = []
        directory?.eachFileRecurse (FileType.FILES) { file ->
            files << file
        }
        files
    }

    def cleanUserHomeDownloadsFolder(){
        def files = getAllFilesFromUserHomeDownloadsFolder()
        files?.each { file ->
            file.delete()
        }
    }

    def verifyExportedFile(fileName, fullFileName){
        def currentFilesList = getAllFilesFromUserHomeDownloadsFolder()
        def finalFilesList = []
        currentFilesList?.each { file ->
            if (file.name.startsWith(fileName)){
                finalFilesList << file
            }
        }
        assert finalFilesList.size() == 1, "System has exported more than one file"
        finalFilesList[0].name == fullFileName
    }
}
