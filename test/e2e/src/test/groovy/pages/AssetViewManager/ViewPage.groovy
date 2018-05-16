package pages.AssetViewManager
import geb.Page
import modules.CommonsModule

class ViewPage extends Page{

    static at = {
        waitFor {view.displayed}
    }
    static content = {
        view (wait:true) { $("section","class":"page-asset-explorer-config")}
        sectionHeaderTitle { $("section.content-header h1")}
        clearBtn {$("button", id:"btnClear")}
        exportViewButton {$("button", id:"btnExport")}
        exportModalContainer {$('#tdsUiDialog')}
        fileNameField {exportModalContainer.find("input", id: "fileName")}
        exportModalButton {exportModalContainer.find("button span", class: "fa-download")}
        cancelModalButton {exportModalContainer.find("button span", class: "glyphicon-ban-circle")}
        commonsModule { module CommonsModule }
    }

    def verifyViewTitle(title) {
        waitFor{sectionHeaderTitle.text().trim() == title}
    }

    def waitForDisplayedModalContainer(){
        waitFor{exportModalContainer.isDisplayed()}
    }

    def waitForHiddenModalContainer(){
        waitFor{!exportModalContainer.isDisplayed()}
    }

    def clickOnExportViewButton() {
        waitFor{exportViewButton.click()}
    }

    def clickOnExportModalButton() {
        waitFor{exportModalButton.click()}
    }

    def clickOnCancelModalButton() {
        waitFor{cancelModalButton.click()}
    }

    def setExportFileName(name) {
        fileNameField = name
    }
}
