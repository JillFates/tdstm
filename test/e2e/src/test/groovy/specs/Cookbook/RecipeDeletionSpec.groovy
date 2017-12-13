package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.TabEditorPage
import pages.Dashboards.UserDashboardPage
import pages.common.LoginPage
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
        at UserDashboardPage
        waitFor { taskMenu.click() }
        cookbookMenuItem.click()
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
        waitFor { gebRecipes.size() == gebRecipeCountBeforeDelete - 1 }
        println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipes.size()
    }

}


