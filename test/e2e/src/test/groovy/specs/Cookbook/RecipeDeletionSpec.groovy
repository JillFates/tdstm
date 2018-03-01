package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class RecipeDeletionSpec extends GebReportingSpec {
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

    def "1. Recipe deletion"() {
        testKey = "TM-7243"
        given: 'The User is on the Cookbook Section'
            at CookbookPage
        when: 'The User searches by the Recipe'
            def gebRecipeCountBeforeDelete = gebRecipes.size()
            println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipeCountBeforeDelete
        and: 'The User clicks the "Delete" Button'
            withConfirm(true) { deleteRecipeButtons[0].click() }
            println "${gebReportingSpecTestName.methodName}: Deleting top most recipe."

        then: 'Recipe count is reduced to 1'
            waitFor { successMessage.present}
            waitFor { !successMessage.present}
            waitFor { loadingIndicator.hasClass("ng-hide")}
            waitFor { gebRecipes.size() == gebRecipeCountBeforeDelete - 1 }
            println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipes.size()
    }
}


