package pages.Projects.ETLScripts

import geb.Page
import modules.CommonsModule

class ETLScriptsDesignerSampleDataPage extends Page{

    static at = {
        modalTitle.text().trim().contains("Sample Data")
    }

    static content = {
        sampleDataContainer {$("data-script-sample-data div.modal-content")}
        modalTitle {sampleDataContainer.find("div", class:"modal-header").find("h4" , class:"modal-title")}
        pasteContentOption {sampleDataContainer.find("label", text: contains("Paste content"))}
        fileFormatDropDown {sampleDataContainer.find("kendo-dropdownlist", id: "fileExtension").find("span", class: "k-select")}
        fileFormatDropDownOptions {$("li[kendodropdownsselectable]")}
        fileContentSampleData {sampleDataContainer.find("textarea", name: "fileContent")}
        uploadSampleDataButton {sampleDataContainer.find("button", text: contains("Upload"))}
        continueSampleDataButton {sampleDataContainer.find("button", text: contains("Continue"))}
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