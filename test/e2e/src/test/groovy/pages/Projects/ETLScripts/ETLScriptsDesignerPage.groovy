package pages.Projects.ETLScripts

import geb.Page
import geb.Browser
import modules.CommonsModule

class ETLScriptsDesignerPage extends Page{

    static at = {
        modalTitle.text().trim().contains("ETL Script Edit")
    }

    static content = {
        modalContainer {$("div.modal-dialog.modal-xl.resize-enabled")}
        modalTitle {modalContainer.find("h3")[0]}
        dsTestButton {$(find("span", text: contains("TEST"))).closest("button")[0]}
        dsCheckSyntaxButton {$(find("span", text: contains("CHECK SYNTAX"))).closest("button")[0]}
        dsLoadDataDesignerButton {$(find("button", text: contains("LOAD SAMPLE DATA")))[0]}
        dsViewConsoleDesignerButton {$(find("button", text: contains("VIEW CONSOLE")))}
        dsCodeContainer {modalContainer.find('div.CodeMirror div textarea')}
        noRecordsRow {modalContainer.find("tr.k-grid-norecords td")}
        dsSaveDesignerButton { $('clr-icon[shape="floppy"]').closest("button")[0]}
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
