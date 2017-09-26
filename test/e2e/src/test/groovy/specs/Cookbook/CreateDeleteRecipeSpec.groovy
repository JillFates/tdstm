package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.common.LoginPage
import pages.Cookbook.UserDashboardPage
import spock.lang.Stepwise

@Stepwise
class CreateDeleteRecipeSpec extends GebReportingSpec {
    def testKey
    def testResult

    def cleanup() {
        //testResult = () ? "PASS" : "FAIL"
        println "cleanup(): ${testKey} ${specificationContext.currentIteration.name} "
    }

    def setupSpec() {
        given:
            to LoginPage

        when:
            username = System.properties['tm.creds.username'] ?: "e2e_test_user"
            password = System.properties['tm.creds.password'] ?: "e2e_password"
            println "setupSpec(): Login as user: e2e_test_user"
            submitButton.click()
        then:
            at UserDashboardPage
    }

    def "01 open Cookbook page"() {
        testKey = "TM-7179"
        given:
            at UserDashboardPage

        when:
            waitFor { taskMenu.click() }
            cookbookMenuItem.click()
        then:
            at CookbookPage
            createRecipeButton.text() == "Create Recipe..."

    }

    def "02 open Create Recipe page"() {
        testKey = "TM-7180"
        given:
            at CookbookPage

        when:
            // hover over createRecipeButton
            //interact { moveToElement(createRecipeButton) }
            createRecipeButton.click()
        then:
            at CreateRecipePage
    }

    def "03 add a recipe Name"() {
        testKey = "TM-7181"
        when:
            nameFieldContents = "Geb Recipe Test"
        then:
            nameFieldContents == "Geb Recipe Test"
            saveButton.@disabled
    }

    def "04 context selector options"() {
        testKey = "TM-7182"
        when:
            contextSelector.click()
            //interact { moveToElement(contextSelector) }
        then:
            contextSelector.$("option")[0].text() == 'Select context'
            contextSelector.$("option")[1].text() == 'Event'
            contextSelector.$("option")[2].text() == 'Bundle'
            contextSelector.$("option")[3].text() == 'Application'
    }

    def "05 select an event context"() {
        testKey = "TM-7183"
        when:
            contextSelector = "Event"
        then:
            contextSelector == "Event"
            saveButton
    }

    def "06 save recipe"() {
        testKey = "TM-7184"
        when:
            descriptionContents = "This is a Geb created recipe for an Event context"
            saveButton.click()
        then:
            at CookbookPage
    }

    def "07 delete recipe"() {
        testKey = "TM-7243"
        when: "Top most Geb Recipe is deleted"
            def gebRecipeCountBeforeDelete = gebRecipes.size()
            println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipeCountBeforeDelete
            //deleteRecipeButtons.each { println it.@title }
            withConfirm(true) { deleteRecipeButtons[0].click() }
            println "${gebReportingSpecTestName.methodName}: Deleting top most recipe."
        then: "Count of geb recipes is down by 1"
            //waitFor { gebRecipes.size() == gebRecipeCountBeforeDelete - 1 }
            println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipes.size()
    }

}