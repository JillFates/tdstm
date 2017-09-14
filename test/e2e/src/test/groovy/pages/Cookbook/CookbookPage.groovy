package pages.Cookbook

import geb.Page

class CookbookPage extends Page {

    static at = {
        title == "Cookbook"
        gebRecipes.size() >= 0
    }

    static content = {
        createRecipeButton {
            // Button uses id which is duplicated on the page.
            // Chrome and Firefox handles duplicate ids differently.
            waitFor { browser.currentUrl.split('/')[-1] == "start" }
            //sleep 1000        // Only needed for Selenium Grid 2.53.1
            if ($("form div a" ).size() > 1) {
                // Two buttons with the same name
                // show up when the Task Generation tab is active
                $("form div a" )[0]
            } else {
                // When other tabs are active
                // the navigator returned is not an array
                //interact { moveToElement( $("form div a") ) }
                //$("#generateTask[ng-click:\"createRecipe()\"]")
                //waitFor {js.('jQuery.active') == 0}
                //waitFor {js.('document.readyState') == 'complete'}
                //waitFor {$("form div a").displayed}
                //waitFor {!js.exec('return jQuery("form div a").is(":animated");')}
                $("form div a" )
            }
        }
        deleteRecipeButtons { $("a.actions.remove" ) }
        gebRecipes(required: false) { $("span.ng-binding", text: "Geb Recipe Test") }
    }
}
