package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CreateRecipePage
import pages.common.LoginPage
import pages.Dashboards.UserDashboardPage
import pages.Cookbook.CookbookPage
import spock.lang.Stepwise

@Stepwise
class CreateRecipeSpec extends GebReportingSpec {

    def setupSpec() {
        given:
            to LoginPage

        when:
            username = "e2e_test_user"
            password = "e2e_password"
            submitButton.click()
            println "setupSpec(): Login as user: ${username}"
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

    def "02 open Create Recipe page"() {
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
        when:
            nameFieldContents = "Geb Recipe Test"
        then:
            nameFieldContents == "Geb Recipe Test"
            saveButton.@disabled
    }

    def "04 context selector options"() {
        when:
            contextSelector.click()
            //interact { moveToElement(contextSelector2) }
        then:
            contextSelector.$("option")[0].text() == 'Select context'
            contextSelector.$("option")[1].text() == 'Event'
            contextSelector.$("option")[2].text() == 'Bundle'
            contextSelector.$("option")[3].text() == 'Application'
    }

    def "05 select an event context"() {
        when:
            contextSelector = "Event"
        then:
            contextSelector == "Event"
            saveButton
    }

    def "06 save recipe"() {
        when:
            descriptionContents = "This is a Geb created recipe for an Event context"
            saveButton.click()
        then:
            at CookbookPage
    }
}