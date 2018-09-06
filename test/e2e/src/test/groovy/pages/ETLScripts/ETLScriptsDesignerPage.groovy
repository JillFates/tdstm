package pages.ETLScripts

import geb.Page
import geb.Browser
import modules.CommonsModule

class ETLScriptsDesignerPage extends Page{

    static at = {
        modalTitle.text().trim().contains("ETL Script Designer")
    }

    static content = {
        modalContainer {$("data-script-etl-builder div.modal-content")}
        modalTitle {modalContainer.find("div", class:"modal-header").find("h4" , class:"modal-title")}
        dsTestButton {modalContainer.find("tds-check-action button", text: contains("Test"))}
        dsCheckSyntaxButton {modalContainer.find("tds-check-action button", text: contains("Check Syntax"))}
        dsLoadDataDesignerButton {modalContainer.find("button", text: contains("Load Sample Data"))}
        dsViewConsoleDesignerButton {modalContainer.find("button", text: contains("View Console"))}
        dsCodeContainer {modalContainer.find('div.CodeMirror div textarea')}
        noRecordsRow {modalContainer.find("tr.k-grid-norecords td")}
        dsSaveDesignerButton {modalContainer.find("button", text: contains("Save"))}
        dsSampleDataRows {$("div.data-preview kendo-grid-list table")[0].find("tr[kendogridlogicalrow]")}
        dsTransformedDataRows {$("div.data-preview kendo-grid-list table")[1].find("tr[kendogridlogicalrow]")}
        commonsModule {module CommonsModule}
    }

    def clickOnDesignerButton(){
        waitFor{dsDesignerButton.click()}
    }

    def clickOnSaveButton(){
        waitFor{dsSaveDesignerButton.click()}
        commonsModule.waitForLoader(2)
    }

    def clickOnTestButton(){
        waitFor{dsTestButton.click()}
        commonsModule.waitForLoader(2)
        commonsModule.waitForLoader(2)
        commonsModule.waitForLoader(2) // loader appears three times
    }

    def clickOnCheckSyntaxButton(){
        waitFor{dsCheckSyntaxButton.click()}
    }

    def getTestButtonVisibility() {
        dsTestButton.@disabled
    }

    def getCheckSyntaxButtonVisibility(){
        dsCheckSyntaxButton.@disabled
    }

    def getViewConsoleButtonVisibility(){
        dsViewConsoleDesignerButton.@disabled
    }

    def getSaveButtonVisibility(){
        dsSaveDesignerButton.@disabled
    }

    def getNoRecordsInSampleDataText(){
        noRecordsRow.text().trim()
    }

    def clickLoadSampleDataButton(){
        waitFor{dsLoadDataDesignerButton.click()}
    }

    def getSampleDataRowsSize(){
        dsSampleDataRows.size()
    }

    def getTransformedDataRowsSize(){
        waitFor{dsTransformedDataRows[0].displayed}
        dsTransformedDataRows.size()
    }

    def setCode(code){
        // enable text area to type
        browser.driver.executeScript("document.querySelectorAll('div.CodeMirror div')[0].style.removeProperty('overflow')")
        dsCodeContainer = code
    }

    def getCheckSyntaxSuccessIcon(){
        waitFor{dsCheckSyntaxButton.find("i", class: "green")}
    }
}
