package pages.Cookbook

import geb.Page

class CookbookPage extends Page {

    static at = {
        title == "Cookbook"
        waitFor(5) { gebRecipes.size() >= 0 }
    }
    static content = {
        createRecipeButton {
            waitFor { browser.currentUrl.split('/')[-1] == "start" }
            if ($("form div a" ).size() > 1) {
                $("form div a" )[0]
            } else {
                $("form div a" )
            }
        }
        deleteRecipeButtons { $("a.actions.remove" ) }
        gebRecipes(required: false) { $("span.ng-binding", text: "Geb Recipe Test") }
    }
}
