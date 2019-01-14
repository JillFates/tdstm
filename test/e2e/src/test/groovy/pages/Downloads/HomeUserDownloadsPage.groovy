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
        resultsTable    (required:true, wait:true){ $("table")}
        fileRow         { resultsTable.find("td a")}
    }

    def waitForExportedFile(fullFileName){
        waitFor {
            driver.navigate().refresh()
            fileRow.find {it.text().contains(fullFileName)}
        }
    }
}