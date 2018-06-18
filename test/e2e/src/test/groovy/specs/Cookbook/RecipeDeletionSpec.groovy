package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import jodd.util.RandomString

@Stepwise
class RecipeDeletionSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "QAE2E"
    static recipeName = baseName + " " + randStr + " Geb Recipe Test"
    static recipeDataMap = [
            name: recipeName,
            context: "Event",
            description: "This is a Geb created recipe for an Event context"
    ]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        menuModule.goToTasksCookbook()
        at CookbookPage
        waitForLoadingIndicator()
        clickOnCreateButton()
        at CreateRecipePage
        createRecipe recipeDataMap
        at CookbookPage
        waitForLoadingIndicator()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Recipe deletion"() {
        testKey = "TM-7243"
        given: 'The User is on the Cookbook Section'
            at CookbookPage
        when: 'The User searches by the Recipe'
            filterByContext recipeDataMap.context // need to play with filtering to refresh grid
            filterByContext "All" // reset to all because AT page checking
            waitFor{ gebRecipes[0].displayed }
        and: 'The User clicks the "Delete" Button'
            withConfirm(wait: true) { deleteRecipeButtons[0].click() }
        then: 'Recipe count is reduced to 1'
            waitFor{ gebRecipes[0].displayed }
            getRecipeByName(recipeDataMap.name) == null
    }
}


