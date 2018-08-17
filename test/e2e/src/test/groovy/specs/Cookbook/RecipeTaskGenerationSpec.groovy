package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.EditRecipePage
import pages.Cookbook.ErrorMessagePage
import pages.Cookbook.TabEditorPage
import pages.Cookbook.TabTaskGenPage
import pages.Cookbook.TabTaskGenTabSummaryPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class RecipeTaskGenerationSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static recipeName = baseName + " " + randStr + " Recipe With Tasks"
    static recipeDataMap = [
            name: recipeName,
            context: "Event",
            description: "This is a Geb created recipe for an Event context"
    ]
    static recipeText = [
            'groups: [',
            '  [',
            '    name: \'QAE2E\',',
            '    description: \'QAE2E list\',',
            '    filter : [',
            '      class: \'application\',',
            '      asset: [',
            '        assetName: \'QAE2E%\'',
            '      ]',
            '    ]',
            '  ]',
            '],',
            'tasks: [',
            '  [',
            '    id: 1100,',
            '    description: \'Startup ALL applications\',',
            '    title: \'Startup app ${it.assetName}\',',
            '    workflow: \'AppStartup\',',
            '    team: \'APP_COORD\',',
            '    category: \'startup\',',
            '    duration: 10,',
            '    filter : [',
            '      group: \'QAE2E\'',
            '    ]',
            '  ]',
            ']'
    ].join('\\n')

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        menuModule.goToTasksCookbook()
        /* CREATE Recipe */
        at CookbookPage
        commonsModule.blockCookbookLoadingIndicator() // disable loading for this spec
        clickOnCreateButton()
        at CreateRecipePage
        createRecipe recipeDataMap
        at CookbookPage
        /* EDIT Recipe */
        openEditTab()
        at TabEditorPage
        clickOnEditButton()
        at EditRecipePage
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

    def "1. Selecting the Recipe to Generate Tasks"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Cookbook Section'
            at CookbookPage
        when: 'The User clicks the Recipe with Task on It'
            waitFor { getRecipeByName(recipeName).click()}
        then: 'Information should be populated'
            getRecipeByName(recipeName) != null
    }

    def "2. Going to The Task Generation tab"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Cookbook Section'
            at CookbookPage
        when: 'The User clicks the Generation Tab'
            waitFor {taskGenerationTab.click() }

        then: 'Generation Section should be displayed'
            at TabTaskGenPage
            waitFor {tskGTab.parent(".active")}
    }

    def "3. Event Dropdown should have the 'Please Select' option selected by default"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Generation Task Section'
            at TabTaskGenPage

        then: 'Event Selector should be blank'
            tskGTabEventSelector == ""
    }

    def "4. Checking Event selector options"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Generation Task Section'
            at TabTaskGenPage
        when: 'The User clicks in the Selector'
            tskGTabEventSelector.click()

        then: 'Options should be more than One'
            tskGTabEventSelector.$("option").size() > 1
    }

    def "5. As Default option should be displayed and shown disabled"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Generation Task Section'
            at TabTaskGenPage

        then: 'Set As Default Option should be displayed and Disabled'
            tskGTabSetDefaultLink.@class !="ng-hide"
            tskGTabSetDefaultLink.@disabled == "true"
            tskGTabSetDefaultLink.text()== "Set as Default"
    }

    def "6. Generate task should be disabled"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Generation Task Section'
            at TabTaskGenPage

        then: 'Generate task should be disabled'
            tskGTabGenerateTasksBtn.@disabled == "true"
    }

    def "7. Selecting an event from the Dropdown"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Generation Task Section'
            at TabTaskGenPage
        when: 'The Event Selector is Buildout'
            tskGTabEventSelector = "Buildout"

        then: 'The User should Select that Option'
            tskGTabEventSelector.find('option', value:tskGTabEventSelector.value()).text().trim() == "Buildout"
    }

    def "8. Setting as Default option should enabled"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Generation Task Section'
            at TabTaskGenPage

        then: 'The Option should be enabled'
            tskGTabSetDefaultLink.@disabled == ""
    }

    def "9. Setting the event as default. Link change to 'Clear Default' "() {
        testKey = "TM-XXXX"
        given: 'The User is in the Generation Task Section'
            at TabTaskGenPage
        when: 'The User clicks the Link'
            waitFor {tskGTabSetDefaultLink.click()}

        then: 'Link should Clear everything out'
            waitFor {tskGTabClearDefaultLink.attr("class") != "ng-hide" }
            tskGTabClearDefaultLink.@disabled == ""
            tskGTabClearDefaultLink.text() == "Clear Default"
    }

    def "10. Checkboxes should be unchecked by default"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Generation Task Section'
            at TabTaskGenPage

        then: 'Checkboxes should be unchecked'
            tskGTabAutoPubTaskCBox.value() == false
            tskGTabGenUsingWipCBox.value() == false
    }

    def "11. Clicking on Generate Task button and error should be displayed"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Generation Task Section'
            at TabTaskGenPage
        when: 'The User clicks the Generate Task Option'
            waitFor {tskGTabGenerateTasksBtn.click()}

        then: 'An Error should be displayed'
            at ErrorMessagePage
    }

    def "12. Validating the error message"() {
        testKey = "TM-XXXX"
        when: 'The Error is displayed'
            at ErrorMessagePage

        then: 'The Error should be visible and certified'
            errorModalText == "There is no released version of the recipe to generate tasks with"
    }

    def "13. Closing error msg"() {
        testKey = "TM-XXXX"
        given: 'The Error is displayed'
            at ErrorMessagePage
        when: 'The User clicks to Close It'
            waitFor {errorModalCloseBtn.click()}

        then: 'The Modal Window should be closed'
            waitFor {errorModalWindow.isDisplayed() == false}
    }

    def "14. Clicking 'Generate using WIP Recipe' checkbox"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Task Generation Section'
            at TabTaskGenPage
        when: 'The User checks the Generate using WIP Option'
            tskGTabGenUsingWipCBox.click()

        then: 'Option should be checked'
            tskGTabGenUsingWipCBox.value() == "on"
    }

    def "15. Generating task button should be enabled"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Task Generation Section'
            at TabTaskGenPage

        then: 'The Generate Task should be enabled'
            tskGTabGenerateTasksBtn.@disabled == ""
    }

    def "16. Generating a Task"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Task Generation Section'
            at TabTaskGenPage
        when: 'The User Clicks on Generate'
            waitFor {tskGTabGenerateTasksBtn.click()}
            waitForProgressBar()
        then: 'The Task should be generated showing up the Summary Section'
            at TabTaskGenTabSummaryPage
    }

    def "17. Checking the Summary Section"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Summary Section'
            at TabTaskGenTabSummaryPage

        then: 'Different values should be displayed'
            waitFor {tskGTabSummaryList.find("li", 0).text().contains("Status: Complete")}
            tskGTabSummaryList.find("li", 1).text().contains("Tasks Created:")
            tskGTabSummaryList.find("li", 2).text().contains("Number of Exceptions:")

    }

    def "18. Clicking on generate task button again should alert the User for tasks previously created"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Task Generation Section'
            at TabTaskGenPage
        when: 'The User clicks the Generate task Button again'
        def resultText
        resultText = withConfirm(false) { tskGTabGenerateTasksBtn.click() }
        then: 'Alert message should be displayed'
            resultText == "There are tasks previously created with this recipe for the selected context.\n" +
                "\n" +
                "Press Okay to delete or Cancel to abort."
    }
}