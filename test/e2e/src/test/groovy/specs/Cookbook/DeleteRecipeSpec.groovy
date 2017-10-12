package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.common.LoginPage
import pages.Dashboards.UserDashboardPage
import spock.lang.Stepwise

@Stepwise
class DeleteRecipeSpec extends GebReportingSpec {

    def setupSpec() {
        given:
            to LoginPage

        when:
            username = "e2e_test_user"
            password = "e2e_password"
            submitButton.click()
        then:
            at UserDashboardPage
    }

    def "01 open Cookbook page"() {
        given:
            at UserDashboardPage

        when:
            waitFor { taskMenu.click() }
            cookbookMenuItem.click()
        then:
            at CookbookPage
            createRecipeButton.text() == "Create Recipe..."

    }

    def "02 List Recipes"() {
        when:
            println "Geb Recipes Count = " + gebRecipes.size()
            gebRecipes.each { println it.text() }
        then:
            gebRecipes.size() > 0
    }

    def "03 Remove Recipe"() {
        when: "Top most Geb Recipe is deleted"
            def gebRecipeCountBeforeDelete = gebRecipes.size()
            println "cleanupSpec(): Geb Recipes count = " + gebRecipeCountBeforeDelete
            //deleteRecipeButtons.each { println it.@title }
            withConfirm(true) { deleteRecipeButtons[0].click() }
            println "cleanupSpec(): Deleting top most recipe."
            println "cleanupSpec(): Geb Recipes count = " + gebRecipes.size()
        then: "There is one less Geb Recipe found"
            gebRecipes.size() == gebRecipeCountBeforeDelete - 1
    }

}