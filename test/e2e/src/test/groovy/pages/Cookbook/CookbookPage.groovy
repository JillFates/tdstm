package pages.Cookbook

import geb.Page

class CookbookPage extends Page {

    static at = {
        waitFor {createRecipeButton.displayed}
        contextSelectorLabel == "Context:"
        contextSelectorDefault.text() == "All"
        createRecipeButton.text() == "Create Recipe..."
        //  TODO following item have the checkbox inside the label
        //  viewArchivedCBoxLabel == "View Archived Recipes"
        taskGenerationTab.text() == "Task Generation"
        historyTab.text() == "History"
        editorTab.text()  == "Editor"
        versionsTab.text() == "Versions"
    }

    static content = {
        pageTitle                   (wait:true) { $("section", 	class:"content-header").find("h1")}
        pageBreadcrumb              { $("o1", class:"breadcrumb")}
        loadingIndicator            (required:false, wait:true){ $("#cookbookRecipesEditor").find("loading-indicator").find("div","ng-show":"isLoading")}
        successMessage              (required:false, wait:true){ $("#cookbookRecipesEditor").find("div.alert.alert-success.animateShow")}
        contextSelectorLabel        { $("label", for:"contextSelector").text()}
        contextSelector             { $("select#contextSelector")}
        contextSelectorDefault      { $("select#contextSelector").find("option", selected:"selected")}
        createRecipeButton          { $("a#generateRecipe")}

        createRecipeModal           (required: false, wait:true) {$ ("div", class:"modal fade in")}
        taskDetailsModal            (required: false, wait:true)  { $("div", "window-class":"modal-task")}
        // TODO following item have the checkbox inside the label
        //     viewArchivedCBoxLabel       { $("label", for:"viewArchived").text()}
        viewArchivedCBox            { $("input#viewArchived")}

        deleteRecipeButtons         { $("a.actions.remove" )}
        taskGenerationTab           { $("li", heading: "Task Generation").find("a")}
        historyTab                  { $("li", heading: "History").find("a")}
        editorTab                   { $("li", heading: "Editor").find("a")}
        versionsTab                 { $("li", heading: "Versions").find("a")}
        recipeGrid                  { $("div", "ng-grid":"recipesGridOptions")}
        recipeGridHeader            { recipeGrid.find("div", class:"ngHeaderContainer")}
        recipeGridHeaderCols        { recipeGridHeader.find("div", "ng-repeat":"col in renderedColumns")}
        recipeGridRows              { recipeGrid.find("div", "ng-repeat":"row in renderedRows")}
        recipeGridRowsCols          { recipeGridRows.find("div", "ng-repeat":"col in renderedColumns")}
        recipeGridRowsActions       { recipeGridRows.find("a", class:"actions")}
        gridSize                    { recipeGridRows.size()}
        rowSize                     { recipeGridHeaderCols.size()}
        gebRecipes                  (required: false) { recipeGridRows.find("div.col0 span.ng-binding")}
    }

    def clickOnCreateButton(){
        waitFor { createRecipeButton.click()}
    }

    def waitForSuccessBanner(){
        waitFor { successMessage.present}
        waitFor { !successMessage.present}
        waitForLoadingIndicator()
    }

    def waitForLoadingIndicator(){
        waitFor { loadingIndicator.hasClass("ng-hide")}
    }

    def openEditTab(){
        editorTab.click()
        waitFor { editorTab.parent(".active") }
    }

    def filterByContext(context){
        def options = contextSelector.find("option")
        def option = options.find {it.text() == context}
        option.click()
    }
}
