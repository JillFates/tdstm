package specs.Tasks.Cookbook

import geb.spock.GebReportingSpec
import pages.Tasks.Cookbook.CookbookPage
import pages.Tasks.Cookbook.CreateRecipePage
import pages.Tasks.Cookbook.TabEditorPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class RecipeCreationSpec extends GebReportingSpec {
    def testKey
    static testCount
    static recipeText

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        tasksModule.goToTasksCookbook()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Opening The Create Recipe page"() {
        given: 'The User is on the Cookbook Section'
            at CookbookPage
            commonsModule.blockCookbookLoadingIndicator() // disable loading for this spec
        when: 'The User clicks the "Create Recipe" Button'
            waitFor { createRecipeButton.click()}

        then: 'The User should be redirected to the Create Recipe Section'
            at CreateRecipePage
    }

    def "2. Check 'Create Recipe' page active elements"() {
        when: 'The User is on the Create Recipe Section'
            at CreateRecipePage

        then: 'Active Elements should be checked'
            saveButton.@disabled == "true"
            nameFieldContents.@required == "true"
            brandNewRecipeTab.parent(".active")
    }

    def "3. Adding a recipe name"() {
        given: 'The User is on the Create Recipe Section'
            at CreateRecipePage
        when: 'The User adds a Name'
            nameFieldContents = "Geb Recipe Test"

        then: 'Create Recipe Name should be created'
            nameFieldContents == "Geb Recipe Test"
        and: 'Save Button should be enabled'
            saveButton.@disabled == ""
    }

    def "4. Adding some description contents"() {
        given: 'The User is on the Create Recipe Section'
            at CreateRecipePage
        when: 'The User adds some description'
            descriptionContents = "This is a Geb created recipe"

        then: 'Save Button should be enabled'
            saveButton.@disable == ""
    }

    def "5. Saving recipe"() {
        def selectedRow = 0
        given: 'The User is on the Create Recipe Section'
            at CreateRecipePage
        when: 'The User Clicks the "Save" button'
            saveButton.click()

        then: 'The User should be redirected to the Cookbook Section'
            at CookbookPage
    }

    def "6. Checking the saved recipe description"() {
        def selectedRow = 0
        when: 'The User is on the Cookbook Section'
            at CookbookPage

        then: 'The information that has been added should be displayed'
            (recipeGridRows[0].find("div", "ng-repeat":"col in renderedColumns"))[1].text().contains("This is a Geb created recipe")
            recipeGridRowsCols.getAt(selectedRow*rowSize + 0).text().trim() == "Geb Recipe Test"
            recipeGridRowsCols.getAt(selectedRow*rowSize+1).text().trim() == "This is a Geb created recipe"
            recipeGridRowsCols.getAt(selectedRow*rowSize+2).text().trim() == "e2e user"
            // TODO next line will check dates for the new recipe. Verify actual local time
            // recipeGridRowsCols.getAt(selectedRow*rowSize+3).text().trim() == now()
            recipeGridRowsCols.getAt(selectedRow*rowSize+4).text().trim() == ""
            recipeGridRowsCols.getAt(selectedRow*rowSize+5).text().trim() == "yes"
    }

    def "7. Check 'Editor' tab selected after recipe is created"() {
        when: 'The User is on the Cookbook Section'
            at CookbookPage

        then: 'Editor Option should be Active'
            editorTab.parent(".active")
            at TabEditorPage
    }
}