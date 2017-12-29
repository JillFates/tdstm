package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.EditRecipePage
import pages.Cookbook.ErrorMessagePage
import pages.Cookbook.TabEditorPage
import pages.Cookbook.TabTaskGenPage
import pages.Cookbook.TabTaskGenTabSummaryPage
import pages.Dashboards.UserDashboardPage
import pages.common.LoginPage
import pages.common.MenuPage
import spock.lang.Stepwise

@Stepwise
class RecipeTaskGenerationSpec extends GebReportingSpec {
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
        at CookbookPage
        waitFor { recipeGridRows.size() > 0 }
        waitFor { createRecipeButton.click()}
        at CreateRecipePage
        nameFieldContents = "Geb Recipe With Tasks Test"
        contextSelector2 = "Event"
        descriptionContents = "This is a Geb created recipe for an Event context"
        saveButton.click()
        at CookbookPage
        waitFor { !createRecipeModal.present }
        editorTab.click()
        waitFor { editorTab.parent(".active") }
        at TabEditorPage
        waitFor { edTabEditorBtn.present }
        edTabEditorBtn.click()
        at EditRecipePage
        def recipe = [
                    'tasks: [',
                    '  [',
                    '    id: 1100,',
                    '    description: \'Startup ALL applications\',',
                    '    title: \'Startup app ${it.assetName}\',',
                    '    workflow: \'AppStartup\',',
                    '    team: \'APP_COORD\',',
                    '    category: \'startup\',',
                    '    duration: 10,',
                    '      filter : [',
                    '        class: \'application\'',
                    '      ],',
                    '  ],',
                    ']'
            ]
        String recipeText = recipe.join('\\n')
        browser.driver.executeScript('return angular.element("#recipeModalSourceCode").scope().modal.sourceCode = "'+recipeText+'"');
        waitFor {editorModalCloseBtn.click()}
        at TabEditorPage
        waitFor {edTabSaveWipBtn.click()}
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "Select the Recipe to Generate Tasks"() {
        testKey = "TM-XXXX"
        given:
        at CookbookPage
        when:
        waitFor { gebRecipesWithTasks.getAt(0).click()}
        then:
        gebRecipesWithTasks.getAt(0).text().trim() == "Geb Recipe With Tasks Test"
    }


    // Task Generation Tab

    def "Go to Task Generation tab"() {
        testKey = "TM-XXXX"
        given:
        at CookbookPage
        when:
        taskGenerationTab.click()
        then:
        at TabTaskGenPage
        waitFor {tskGTab.parent(".active")}
    }

    def "Event Dropdown should have 'Please Select' option selected by default"() {
        testKey = "TM-XXXX"
        when:
        at TabTaskGenPage
        then:
        tskGTabEventSelector == ""
    }

    def "Check Event selector options"() {
        testKey = "TM-XXXX"
        given:
        at TabTaskGenPage
        when:
        tskGTabEventSelector.click()
        then:
        tskGTabEventSelector.$("option").size() > 1
    }

    def "Set as Default option should be displayed and disabled"() {
        testKey = "TM-XXXX"
        when:
        at TabTaskGenPage
        then:
        tskGTabSetDefaultLink.@class !="ng-hide"
        tskGTabSetDefaultLink.@disabled == "true"
        tskGTabSetDefaultLink.text()== "Set as Default"
    }

    def "Generate task should be disabled"() {
        testKey = "TM-XXXX"
        when:
        at TabTaskGenPage
        then:
        tskGTabGenerateTasksBtn.@disabled == "true"
    }

    def "Select an event from the Dropdown"() {
        testKey = "TM-XXXX"
        given:
        at TabTaskGenPage
        when:
        tskGTabEventSelector = "Buildout"
        then:
        tskGTabEventSelector.find('option', value:tskGTabEventSelector.value()).text().trim() == "Buildout"
    }

    def "Set as Default option should enabled"() {
        testKey = "TM-XXXX"
        when:
        at TabTaskGenPage
        then:
        tskGTabSetDefaultLink.@disabled == ""
    }

    def "Set the event as default. Link change to 'Clear Default' "() {
        testKey = "TM-XXXX"
        given:
        at TabTaskGenPage
        when:
        waitFor {tskGTabSetDefaultLink.click()}
        then:
        waitFor {tskGTabClearDefaultLink.attr("class") != "ng-hide" }
        tskGTabClearDefaultLink.@disabled == ""
        tskGTabClearDefaultLink.text() == "Clear Default"
    }

    // Automatically publish tasks Checkbox

    def "Checkboxes should be unchecked by default"() {
        testKey = "TM-XXXX"
        when:
        at TabTaskGenPage
        then:
        tskGTabAutoPubTaskCBox.value() == false
        tskGTabGenUsingWipCBox.value() == false
    }

    def "Click on Generate Task button and error should be displayed"() {
        testKey = "TM-XXXX"
        given:
        at TabTaskGenPage
        when:
        waitFor {tskGTabGenerateTasksBtn.click()}
        then:
        at ErrorMessagePage
    }

    def "should validate error message"() {
        testKey = "TM-XXXX"
        when:
        at ErrorMessagePage
        then:
        errorModalText == "There is no released version of the recipe to generate tasks with"

    }

    def "Close error msg"() {
        testKey = "TM-XXXX"
        given:
        at ErrorMessagePage
        when:
        waitFor {errorModalCloseBtn.click()}
        then:
        waitFor {errorModalWindow.isDisplayed() == false}
    }

    def "Click 'Generate using WIP Recipe' checkbox"() {
        testKey = "TM-XXXX"
        given:
        at TabTaskGenPage
        when:
        tskGTabGenUsingWipCBox.click()
        then:
        tskGTabGenUsingWipCBox.value() == "on"
    }

    def "Generate task button should be enabled"() {
        testKey = "TM-XXXX"
        when:
        at TabTaskGenPage
        then:
        tskGTabGenerateTasksBtn.@disabled == ""
    }

    def "Generate task"() {
        testKey = "TM-XXXX"
        given:
        at TabTaskGenPage
        when:
        waitFor {tskGTabGenerateTasksBtn.click()}
        then:
        at TabTaskGenTabSummaryPage
    }

    def "Check Summary"() {
        testKey = "TM-XXXX"
        when:
        at TabTaskGenTabSummaryPage
        then:
        waitFor {tskGTabSummaryList.find("li", 0).text().contains("Status: Complete")}
        tskGTabSummaryList.find("li", 1).text().contains("Tasks Created:")
        tskGTabSummaryList.find("li", 2).text().contains("Number of Exceptions:")

    }
    // Generate Tasks with previously tasks created

    def "Click on generate task button again alerts for tasks previously created"() {
        testKey = "TM-XXXX"
        given:
        at TabTaskGenPage
        when:
        def resultText
        resultText = withConfirm(false) { tskGTabGenerateTasksBtn.click() }
        then:
        resultText == "There are tasks previously created with this recipe for the selected context.\n" +
                "\n" +
                "Press Okay to delete or Cancel to abort."
    }
}


