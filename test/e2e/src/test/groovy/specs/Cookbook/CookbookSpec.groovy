package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.TabHistoryPage
import pages.Dashboards.UserDashboardPage
import pages.common.LoginPage
import spock.lang.Stepwise

@Stepwise
class CookbookSpec extends GebReportingSpec {
    def testKey
    static testCount
    static recipeText

    def setupSpec() {
        testCount = 0
        def username = "e2e_test_user"
        def password = "e2e_password"
        to LoginPage
        loginModule.login(username,password)
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    // Enter on Cookbook page
    def "Go to Cookbook page"() {
        testKey = "TM-7179"
        given:
        at UserDashboardPage
        when:
        waitFor { taskMenu.click() }
        cookbookMenuItem.click()
        then:
        at CookbookPage
        waitFor { recipeGridRows.size() > 0 }
    }

    def "Check Cookbook page active elements"() {
        testKey = "TM-XXXX"
        when:
        at CookbookPage
        then:
        pageTitle.text().trim() == "Cookbook"
        taskGenerationTab.parent(".active")
        recipeGridHeaderCols.getAt(0).text().trim() == "Recipe"
        recipeGridHeaderCols.getAt(1).text().trim() == "Description"
        recipeGridHeaderCols.getAt(2).text().trim() == "Context"
        recipeGridHeaderCols.getAt(3).text().trim() == "Editor"
        recipeGridHeaderCols.getAt(4).text().trim() == "Last Updated"
        recipeGridHeaderCols.getAt(5).text().trim() == "Version"
        recipeGridHeaderCols.getAt(6).text().trim() == "WIP"
        recipeGridHeaderCols.getAt(7).text().trim() == "Actions"
    }

}


