package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class RecipeDeletionSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static recipeName = baseName + " " + randStr + " Geb Recipe Test"
    static recipeDataMap = [
            name: recipeName,
            context: "Event",
            description: "This is a Geb created recipe for an Event context"
    ]
    static maxNumberOfBulkRecipesToBeDeleted = 4 // custom E2E recipe to remove in workaround test
    static recipesNameList = [baseName, "Geb Recipe Test"]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        menuModule.goToTasksCookbook()
        at CookbookPage
        commonsModule.blockCookbookLoadingIndicator() // disable loading for this spec
        clickOnCreateButton()
        at CreateRecipePage
        createRecipe recipeDataMap
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Recipe deletion"() {
        given: 'The User is on the Cookbook Section'
            at CookbookPage
        when: 'The User searches by the Recipe'
            waitFor{ gebRecipes[0].displayed }
        and: 'The User clicks the "Delete" Button'
            withConfirm(wait: true) { deleteRecipeButtons[0].click() }
            waitForSuccessMessage()
        then: 'Recipe count is reduced to 1'
            waitFor{ gebRecipes[0].displayed }
            getRecipeByName(recipeDataMap.name) == null
    }

    def "2. Workaround to delete custom E2E tags"(){
        when: 'The User waits first recipe is displayed in grid'
            waitForFirstRecipeDisplayed()
        then: 'The user deletes custom E2E tags if there are'
            bulkDelete maxNumberOfBulkRecipesToBeDeleted, recipesNameList
    }
}


