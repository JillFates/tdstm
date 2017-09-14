package pages.Cookbook

import geb.Page

class CreateRecipePage extends Page {

    static at = {
        modalTitle.text() == "Create a recipe"
        brandNewRecipeTab.text() == "Brand New Recipe"
        brandNewRecipeTab.parent(".active")
        cloneExistingRecipeTab.text() == "Clone An Existing Recipe"
        saveButton.@disabled
        cancelButton
        nameFieldLabel == "Name*"
        nameFieldContents.text() == ""
        nameFieldContents.@required
        contextSelectorLabel == "Context*"
        contextSelector.@required
        descriptionLabel == "Description"
        descriptionContents == ""
    }

    static content = {
        modalTitle(wait:true) { $("div", class:"modal-header").find("h3") }
        brandNewRecipeTab { $("li", heading: "Brand New Recipe").find("a") }
        cloneExistingRecipeTab { $("li", heading: "Clone An Existing Recipe").find("a") }
        //saveButton { $("button", "ng-click":"save()" ) }
        saveButton { $("button", text:"Save" ) }
        cancelButton { $("button", "ng-click":"cancel()" ) }
        nameFieldLabel { $("label", for:"inputName").text() }
        nameFieldContents { $("input#inputName") }
        contextSelectorLabel { $("label", for:"contextSelector2").text() }
        contextSelector { $("select#contextSelector2") }
        descriptionLabel { $("label", for:"textareaDescription").text() }
        descriptionContents { $("textarea#textareaDescription") }
    }
}
