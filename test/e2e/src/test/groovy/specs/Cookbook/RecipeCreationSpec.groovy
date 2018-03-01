package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.TabEditorPage
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
        menuModule.goToTasksCookbook()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Opening The Create Recipe page"() {
        testKey = "TM-7180"
        given: 'The User is on the Cookbook Section'
            at CookbookPage
        when: 'The User clicks the "Create Recipe" Button'
            waitFor { createRecipeButton.click()}

        then: 'The User should be redirected to the Create Recipe Section'
            at CreateRecipePage
    }

    def "2. Check 'Create Recipe' page active elements"() {
        testKey = "TM-XXXX"
        when: 'The User is on the Create Recipe Section'
            at CreateRecipePage

        then: 'Active Elements should be checked'
            saveButton.@disabled == "true"
            nameFieldContents.@required == "true"
            contextSelector2.@required == "true"
            brandNewRecipeTab.parent(".active")
    }

    def "3. Adding a recipe name"() {
        testKey = "TM-7181"
        given: 'The User is on the Create Recipe Section'
            at CreateRecipePage
        when: 'The User adds a Name'
            nameFieldContents = "Geb Recipe Test"

        then: 'Create Recipe Name should be created'
            nameFieldContents == "Geb Recipe Test"
            saveButton.@disabled == "true"
    }

    def "4. Checking 'Context' selector options"() {
        testKey = "TM-7182"
        given: 'The User is on the Create Recipe Section'
            at CreateRecipePage
        when: 'The User searches by the Context Selector'
            contextSelector2.click()

        then: 'Event, Bundle and Application options should be displayed'
            contextSelector2.$("option")[0].text() == 'Select context'
            contextSelector2.$("option")[1].text() == 'Event'
            contextSelector2.$("option")[2].text() == 'Bundle'
            contextSelector2.$("option")[3].text() == 'Application'
    }

    def "5. Selecting the 'Event' context"() {
        testKey = "TM-7183"
        when: 'Selecting the Event Element'
            contextSelector2 = "Event"

        then: 'Event should be selected'
            contextSelector2 == "Event"
    }

    def "6. Checking the 'Save' Button status"() {
        testKey = "TM-XXXX"
        when: 'The User is on the Create Recipe Section'
            at CreateRecipePage

        then: 'Save Button should be disabled'
            saveButton.@disabled == ""
    }

    def "7. Adding some description contents"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Create Recipe Section'
            at CreateRecipePage
        when: 'The User adds some description'
            descriptionContents = "This is a Geb created recipe for an Event context"

        then: 'Save Button should be disabled'
            saveButton.@disable == ""
    }

    def "8. Saving recipe"() {
        testKey = "TM-7184"
        def selectedRow = 0
        given: 'The User is on the Create Recipe Section'
            at CreateRecipePage
        when: 'The User Clicks the "Save" button'
            saveButton.click()

        then: 'The User should be redirected to the Cookbook Section'
            at CookbookPage
    }

    def "9. Checking the saved recipe description"() {
        testKey = "TM-7184"
        def selectedRow = 0
        when: 'The User is on the Cookbook Section'
            at CookbookPage

        then: 'The information that has been added should be displayed'
            (recipeGridRows[0].find("div", "ng-repeat":"col in renderedColumns"))[1].text().contains("This is a Geb created recipe for an Event context")
            recipeGridRowsCols.getAt(selectedRow*rowSize + 0).text().trim() == "Geb Recipe Test"
            recipeGridRowsCols.getAt(selectedRow*rowSize+1).text().trim() == "This is a Geb created recipe for an Event context"
            recipeGridRowsCols.getAt(selectedRow*rowSize+2).text().trim() == "Event"
            recipeGridRowsCols.getAt(selectedRow*rowSize+3).text().trim() == "e2e user"
            // TODO next line will check dates for the new recipe. Verify actual local time
            // recipeGridRowsCols.getAt(selectedRow*rowSize+4).text().trim() == now()
            recipeGridRowsCols.getAt(selectedRow*rowSize+5).text().trim() == ""
            recipeGridRowsCols.getAt(selectedRow*rowSize+6).text().trim() == "yes"
    }

    def "10. Check 'Editor' tab selected after recipe is created"() {
        testKey = "TM-XXXX"
        when: 'The User is on the Cookbook Section'
            at CookbookPage

        then: 'Editor Option should be Active'
            editorTab.parent(".active")
            at TabEditorPage
    }
}