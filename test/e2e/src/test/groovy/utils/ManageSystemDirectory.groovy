package utils

import groovy.io.FileType

class ManageSystemDirectory {

    def waitForDownloadedFile(fileName){
        def count = 0
        def file = getAllFilesFromTempDownloadsFolder()[0]
        while(!file.name.startsWith(fileName) && count < 5) {
            file = getAllFilesFromTempDownloadsFolder()[0]
            Thread.sleep(1000)
            count++
        }
        assert file.name.startsWith(fileName), "File download has exceeded 5 seconds"
    }

    def getDownloadsFolder(){
        def userHome = System.getProperty("user.home")
        new File(userHome + "/Downloads/")
    }

    def createTempDownloadsFolder(){
        def downloadsDir = getDownloadsFolder()
        deleteTempDownloadsFolder()
        downloadsDir.mkdir()
    }

    def deleteTempDownloadsFolder(){
        def downloadsDir = getDownloadsFolder()
        if (downloadsDir.exists()){
            downloadsDir.delete()
        }
    }

    def getAllFilesFromTempDownloadsFolder(){
        def directory = getDownloadsFolder()
        def files = []
        directory?.eachFileRecurse (FileType.FILES) { file ->
            files << file
        }
        files
    }

    def verifyExportedFile(fileName, fullFileName){
        def currentFilesList = getAllFilesFromTempDownloadsFolder()
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