package pages.Tasks.Cookbook

import geb.Page

class EditRecipePage extends Page {

    static at = {
        editorModalWindow.isDisplayed()
        editorModalCloseBtn //TODO check if enabled
        editorModalCloseBtn.text() == "Close"
        editorModalCancelBtn //TODO check if enabled
        editorModalCancelBtn.text() == "Cancel"
    }

    static content = {
        loadingIndicator                { $("div", "ng-show": "isLoading")}
        editorModalWindow(wait:true)    { $("div", "window-class":"code-editor-modal")}
        editorModalArea                 { $("div#recipeModalSourceCode")}
        editorModalTextArea             { editorModalWindow.find("div", class: "CodeMirror-code").find("pre")}
        editorModalCloseBtn(wait:true)  { $("button", "ng-click": "storeCode()")}
        editorModalCancelBtn(wait:true) { $("button", "ng-click": "cancel()")}
    }
}
