package pages.Tasks.Cookbook

import geb.Page

class TabEditorPage extends Page {

    static at = {
        editorTab.parent(".active")
        //TODO check when tab is active and verify the recipe name first on the list
        //      edTabRecipeName == recipeName:
        //      edTabRecipeLabel == "Recipe:"
        edTabEditorBtn.text() == "Edit"
        edTabSaveWipBtn.text()  == "Save WIP"
        edTabUndoBtn.text() == "Undo"
        edTabReleaseBtn.text() == "Release"
        edTabDiscardWipBtn.text() == "Discard WIP"
        edTabDiffBtn.text() == "Diff"
        edTabCheckSyntaxBtn.text() == "Check Syntax"
    }

    static content = {
        editorTab(wait:true)            { $("li", heading: "Editor").find("a")}
        edTabRecipeName                 { $("span", class: "headingTitle col-xs-6").find("p")}
        edTabRecipeLabel                { $("label", "ng-show": "editor.selectedRVersion.name")}
        edTabTextArea                   { $("div", class:"CodeMirror-lines").find("div", class: "CodeMirror-code")}
        edTabEditorBtn(wait:true)       { $("button", "ng-click": "showEditPopup()")}
        edTabSaveWipBtn(wait:true)      { $("button", "ng-click": "saveWIP()")}
        edTabUndoBtn                    { $("button", "ng-click": "cancelChanges()")}
        edTabReleaseBtn                 { $("button", "ng-click": "releaseVersion()")}
        edTabDiscardWipBtn              { $("button", "ng-click": "discardWIP()")}
        edTabDiffBtn                    { $("button", "ng-click": "diff()")}
        edTabCheckSyntaxBtn(wait:true)  { $("button", "ng-click": "validateSyntax()")}
        edTabChangeLogsTab              { $("li", heading: "Change Logs").find("a")}
        edTabGroupsTab                  { $("li", heading: "Groups").find("a")}
    }

    def clickOnEditButton(){
        waitFor { edTabEditorBtn.present }
        edTabEditorBtn.click()
    }
}
