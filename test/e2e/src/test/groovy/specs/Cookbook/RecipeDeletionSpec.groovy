package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.common.LoginPage
import pages.common.MenuPage
import spock.lang.Stepwise

@Stepwise
class RecipeDeletionSpec extends GebReportingSpec {
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

    def "Delete Recipe"() {
        testKey = "TM-7243"
        given:
        at CookbookPage
        when: "Top most Geb Recipe is deleted"
        def gebRecipeCountBeforeDelete = gebRecipes.size()
        println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipeCountBeforeDelete
        withConfirm(true) { deleteRecipeButtons[0].click() }
        println "${gebReportingSpecTestName.methodName}: Deleting top most recipe."
        then: "Count of geb recipes is down by 1"
        waitFor { successMessage.present}
        waitFor { !successMessage.present}
        waitFor { loadingIndicator.hasClass("ng-hide")}
        waitFor { gebRecipes.size() == gebRecipeCountBeforeDelete - 1 }
        println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipes.size()
    }

}


