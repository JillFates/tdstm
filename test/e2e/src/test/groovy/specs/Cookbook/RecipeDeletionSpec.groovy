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
    static maxNumberOfBulkRecipesToBeDeleted = 1 // custom E2E recipe to remove in workaround test
    static recipesNameList = [baseName, "Geb Recipe Test"]

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

    def "1. Workaround to delete custom E2E tags"(){
        given: 'The User is on the Cookbook Section'
            at CookbookPage
            commonsModule.blockCookbookLoadingIndicator() // disable loading for this spec
        when: 'The User waits first recipe is displayed in grid'
            waitForFirstRecipeDisplayed()
        then: 'The user deletes custom E2E tags if there are'
            bulkDelete maxNumberOfBulkRecipesToBeDeleted, recipesNameList
    }
}


