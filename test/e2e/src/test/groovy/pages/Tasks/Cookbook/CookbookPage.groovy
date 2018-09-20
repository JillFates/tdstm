package pages.Tasks.Cookbook

import geb.Page
import modules.CommonsModule
import geb.waiting.WaitTimeoutException

class CookbookPage extends Page {

    static at = {
        waitFor {createRecipeButton.displayed}
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
        successMessage              { $("#cookbookRecipesEditor").find("div.alert.alert-success.animateShow")}
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
        gebRecipes                  (required: false) { recipeGridRows.find("div.ngCellText.col0")}
        taskGenerationTabContent { $("div[ui-view=taskBatchStart]")}
        taskGenerationTabRecipeName { taskGenerationTabContent.find("p")}
        commonsModule { module CommonsModule }
    }

    def clickOnCreateButton(){
        waitFor { createRecipeButton.click()}
    }

    def openEditTab(){
        waitFor { editorTab.click()}
        waitFor { editorTab.parent(".active") }
    }

    def getRecipeByName(name){
        gebRecipes.find{it.text().trim() == name}
    }

    def getAllRecipesContainingName(name){
        gebRecipes.findAll{it.text().trim().contains(name)}
    }

    def getRecipeById(id){
        deleteRecipeButtons.find{it.attr("recipe-id") == id}
    }

    def getRecipeNameDisplayedInTaskGenerationTab(){
        taskGenerationTabRecipeName.text()
    }

    def deleteRecipeByGivenSelector(recipeNameSelector){
        def recipeName = recipeNameSelector.text().trim()
        def recipeDeleteButton = recipeNameSelector.parents("div.ngCell").nextAll().find("a.actions.remove")
        def recipeId = recipeDeleteButton.attr("recipe-id")
        withConfirm(wait: true) {
            recipeDeleteButton.click()
        }
        waitForSuccessMessage()
        assert getRecipeById(recipeId) == null, "${recipeName} recipe with id ${recipeId} still displayed"
    }

    def waitForSuccessMessage(){
        def displayed = false
        try {
            waitFor(2){successMessage.displayed}
            displayed = true
            waitFor(2){!successMessage.displayed}
        } catch (WaitTimeoutException e){
            // we are good, banner is not present in html
            assert displayed
        }
    }

    def getRecipeContainingNameFromGivenList(recipeNames){
        def found
        for (name in recipeNames){
            def recipes = getAllRecipesContainingName name
            if (recipes) {
                found = recipes[0]
                break
            }
        }
        found
    }

    def waitForFirstRecipeDisplayed(){
        waitFor{gebRecipes[0].displayed}
    }

    /**
     * Bulk delete recipes
     * @param maxNumberOfBulkRecipesToBeDeleted
     * @param recipeNames = list of names to find and delete
     * @author Sebastian Bigatton
     */
    def bulkDelete(maxNumberOfBulkRecipesToBeDeleted, recipeNames) {
        def count = 0
        while (getRecipeContainingNameFromGivenList(recipeNames) != null) {
            count = count + 1
            if (count > maxNumberOfBulkRecipesToBeDeleted) {
                break
            }
            def deletedRecipe = false
            recipeNames.each{ recipe ->
                def recipeToBeDeletedSelector = getRecipeByName recipe
                if (recipeToBeDeletedSelector && !deletedRecipe) {
                    deleteRecipeByGivenSelector recipeToBeDeletedSelector
                    deletedRecipe = true
                }
            }
        }
        true // done, just return true to avoid test fails
    }
}
