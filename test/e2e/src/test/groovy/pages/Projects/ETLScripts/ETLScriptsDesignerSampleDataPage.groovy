package pages.Projects.ETLScripts

import geb.Page
import modules.CommonsModule

class ETLScriptsDesignerSampleDataPage extends Page{

    static at = {
        modalTitle.text().trim().contains("Sample Data")
    }

    static content = {
        sampleDataContainer {$(".modal-dialog.modal-lg")}
        modalTitle {sampleDataContainer.find("h3")}
        pasteContentOption {sampleDataContainer.find("label", text: contains("Paste content"))}
        fileFormatDropDown (required:false){sampleDataContainer.find("kendo-dropdownlist", id: "fileExtension").find("span", class: "k-select")}
        fileFormatDropDownOptions (required:false){$("li[kendodropdownsselectable]")}
        fileContentSampleData (required:false){sampleDataContainer.find("textarea", name: "fileContent")}
        uploadSampleDataButton (required:false){sampleDataContainer.find("button", text: contains("UPLOAD"))}
        continueSampleDataButton {sampleDataContainer.find("button", text: contains("CONTINUE"))}
        commonsModule {module CommonsModule}
    }

    def clickOnPasteContent(){
        waitFor{pasteContentOption.click()}
    }

    def clickOnUploadButton(){
        waitFor{uploadSampleDataButton.click()}
    }

    def clickOnContinueButton(){
        waitFor{continueSampleDataButton.click()}
        commonsModule.waitForLoader(5) // waiting modal close and loads data in DS Designer
    }

    def selectFormatFromDropdown(fileFormat){
        waitFor{fileFormatDropDown.click()}
        waitFor{fileFormatDropDownOptions.find{it.text().contains(fileFormat)}.click()}
    }

    def setContent(content){
        fileContentSampleData = content
    }
}