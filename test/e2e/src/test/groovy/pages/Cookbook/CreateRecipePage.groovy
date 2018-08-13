package pages.Cookbook

import geb.Page
import modules.CommonsModule

class CreateRecipePage extends Page {

    static at = {
        modalTitle.text() == "Create a recipe"
        brandNewRecipeTab.text() == "Brand New Recipe"
        cloneExistingRecipeTab.text() == "Clone An Existing Recipe"
        cancelButton
        nameFieldLabel == "Name*"
        nameFieldContents.text() == ""
        descriptionLabel == "Description"
    }

    static content = {
        modalTitle(wait:true)   { $("div", class:"modal-header").find("h3")}
        brandNewRecipeTab       { $("li", heading: "Brand New Recipe").find("a")}
        cloneExistingRecipeTab  { $("li", heading: "Clone An Existing Recipe").find("a")}
        saveButton              { $("button", "ng-click":"save()")}
        cancelButton            { $("button", "ng-click":"cancel()")}
        nameFieldLabel          { $("label", for:"inputName").text()}
        nameFieldContents       { $("input#inputName")}
        descriptionLabel        { $("label", for:"textareaDescription").text()}
        descriptionContents     { $("textarea#textareaDescription")}
        commonsModule { module CommonsModule }
    }

    def createRecipe(recipeDataMap){
        nameFieldContents = recipeDataMap.name
        nameFieldContents == recipeDataMap.name
        descriptionContents = recipeDataMap.description
        saveButton.click()
    }
}
