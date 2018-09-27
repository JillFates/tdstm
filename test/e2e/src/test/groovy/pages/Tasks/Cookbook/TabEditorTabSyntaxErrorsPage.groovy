package pages.Tasks.Cookbook

import geb.Page

class TabEditorTabSyntaxErrorsPage extends Page {

    static at = {
        SyntErrTab.parent(".active")
    }

    static content = {

        SyntErrTab                      { $("li", heading: "Syntax Errors").find("a") }
        SyntErrTabTitle(wait:true)      { $("p", "ng-bind": "error.reason") }
        SyntErrTabDetails(wait:true)    { $("p", "ng-bind-html": "secureHTML(error.detail)") }
        loadingIndicator(wait:true)     { $("div", "ng-show": "isLoading")}
    }
}
