package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class CookbookSpec extends GebReportingSpec {
    def testKey
    static testCount
    static recipeText

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Going to the Cookbook Section"() {
        testKey = "TM-7179"
        given: 'The User is at the Menu Page'
            at MenuPage
        when: 'The User Goes to the Tasks > Cookbook Section'
            menuModule.goToTasksCookbook()

        then: 'Cookbook Section should be displayed'
            at CookbookPage
            waitFor { recipeGridRows.size() > 0 }
    }

    def "2. Checking Cookbook page active elements"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Cookbook Section'
            at CookbookPage
            commonsModule.blockCookbookLoadingIndicator() // disable loading for this spec
        then: 'Active Elements should be present'
            pageTitle.text().trim() == "Cookbook"
            taskGenerationTab.parent(".active")
            recipeGridHeaderCols.getAt(0).text().trim() == "Recipe"
            recipeGridHeaderCols.getAt(1).text().trim() == "Description"
            recipeGridHeaderCols.getAt(2).text().trim() == "Editor"
            recipeGridHeaderCols.getAt(3).text().trim() == "Last Updated"
            recipeGridHeaderCols.getAt(4).text().trim() == "Version"
            recipeGridHeaderCols.getAt(5).text().trim() == "WIP"
            recipeGridHeaderCols.getAt(6).text().trim() == "Actions"
    }
}