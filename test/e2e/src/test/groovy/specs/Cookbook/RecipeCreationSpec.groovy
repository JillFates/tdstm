package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.TabEditorPage
import pages.common.LoginPage
import pages.common.MenuPage
import spock.lang.Stepwise

@Stepwise
class RecipeCreationSpec extends GebReportingSpec {
    def testKey
    static testCount
    static recipeText

    def setupSpec() {
        testCount = 0
        def username = "e2e_test_user"
        def password = "e2e_password"
        to LoginPage
        loginModule.login(username,password)
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

    def "Open 'Create Recipe' page"() {
        testKey = "TM-7180"
        given:
        at CookbookPage
        when:
        waitFor { createRecipeButton.click()}
        then:
        at CreateRecipePage
    }

    def "Check 'Create Recipe' page active elements"() {
        testKey = "TM-XXXX"
        when:
        at CreateRecipePage
        then:
        saveButton.@disabled == "true"
        nameFieldContents.@required == "true"
        contextSelector2.@required == "true"
        brandNewRecipeTab.parent(".active")
    }

    def "Add a recipe name"() {
        testKey = "TM-7181"
        given:
        at CreateRecipePage
        when:
        nameFieldContents = "Geb Recipe Test"
        then:
        nameFieldContents == "Geb Recipe Test"
        saveButton.@disabled == "true"
    }

    def "Check 'Context' selector options"() {
        testKey = "TM-7182"
        given:
        at CreateRecipePage
        when:
        contextSelector2.click()
        then:
        contextSelector2.$("option")[0].text() == 'Select context'
        contextSelector2.$("option")[1].text() == 'Event'
        contextSelector2.$("option")[2].text() == 'Bundle'
        contextSelector2.$("option")[3].text() == 'Application'
    }

    def "Select an 'Event' context"() {
        testKey = "TM-7183"
        when:
        contextSelector2 = "Event"
        then:
        contextSelector2 == "Event"
    }

    def "Check the 'Save' Button status"() {
        testKey = "TM-XXXX"
        when:
        at CreateRecipePage
        then:
        saveButton.@disabled == ""
    }

    def "Add description contents"() {
        testKey = "TM-XXXX"
        given:
        at CreateRecipePage
        when:
        descriptionContents = "This is a Geb created recipe for an Event context"
        then:
        saveButton.@disable == ""
    }

    def "Save recipe"() {
        testKey = "TM-7184"
        def selectedRow = 0
        given:
        at CreateRecipePage
        when:
        saveButton.click()
        then:
        at CookbookPage
    }

    def "Check the saved recipe description"() {
        testKey = "TM-7184"
        def selectedRow = 0
        when:
        at CookbookPage

        then:
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

    def "Check 'Editor' tab selected after recipe is created"() {
        testKey = "TM-XXXX"
        when:
        at CookbookPage
        then:
        editorTab.parent(".active")
        at TabEditorPage
    }

}


