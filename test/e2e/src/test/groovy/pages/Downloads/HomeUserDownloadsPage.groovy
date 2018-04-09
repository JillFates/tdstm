package pages.Downloads

import geb.Page
import geb.Browser

class HomeUserDownloadsPage extends Page {

    /*
    * TODO: implement a way to know if its running local machine or server
    * Default url is set to look the file in server download folder
    * To run local machine use your path to Downloads folder in user home
    * For example: "file:///C:/Users/Sebastian/Downloads"
    */
    static url = "file:///home/seluser/Downloads"

    static at = {
        resultsTable.isDisplayed()
    }

    static content = {
        resultsTable (required:true, wait:true){ $("table")}
    }

    def getFileRow(fullFileName){
        waitFor{ resultsTable }
        def fileRow = resultsTable.find("td a")
        fileRow.find {it.text().contains(fullFileName)}
    }

    def verifyExportedFile(fullFileName){
        def count = 0
        def fileText = getFileRow(fullFileName).text().toString()
        while(!fileText.equals(fullFileName.toString()) && count < 5) {
            // refresh page to refresh table results
            Browser.drive { go url }
            fileText = getFileRow(fullFileName).text().toString()
            Thread.sleep(1000)
            count++
        }
        assert count < 5, "File download has exceeded 5 seconds"
        fileText == fullFileName.toString()
    }
}