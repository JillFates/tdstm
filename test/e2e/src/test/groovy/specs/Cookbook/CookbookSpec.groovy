package specs.Cookbook

import geb.spock.GebReportingSpec
import javafx.scene.control.Alert
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.EditRecipePage
import pages.Cookbook.ErrorMessagePage
import pages.Cookbook.TabEditorPage
import pages.Cookbook.TabEditorTabSyntaxErrorsPage
import pages.Cookbook.TabTaskGenPage
import pages.Cookbook.TabTaskGenTabSummaryPage
import pages.Dashboards.UserDashboardPage
import pages.common.LoginPage
import spock.lang.Stepwise

//TM-2995  Unable to regenerate tasks after reverting and deleting version of a recipe
/*TM-2912 
user should not be able to delete the release version
delete the WIP / should only delete the WIP and not the release version
If the user delete the WIP and  this is the only recipe version , the editor should be cleared.
When you create a recipe description is not required, but it is required when you clone a recipe. - should not have the same behavior on both?
# tabs are not displayed if recipe list is is empty
# if you have 1 or more recipes, the  tabs are displayed and the 1st recipe is selected.

#  Task Generation 
 checkbox : 
 Generate using WIP recipe - > is only displayed when you select a recipe with wip created
 Delete previously generated tasks that were created using this context & recipe ->  This is only displayed if task are created using that wip recipe 
 # Clone create recipe -> description is not mandatory
*/

@Stepwise
class CookbookSpec extends GebReportingSpec {
    def testKey
    static testCount
    static recipeText

    def setupSpec() {
        testCount = 0
        //TODO put the following values on a property file
        def userName = System.properties['tm.creds.username'] ?: "e2e_test_user"
        def passWord = System.properties['tm.creds.password'] ?: "e2e_password"

        given:
        //TODO move the Login process to own Page and Spec
        to LoginPage

        when:
        username = userName
        password = passWord
        println "setupSpec(): Login as user: ${userName}"
        submitButton.click()

        then:
        at UserDashboardPage
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
        waitFor(5) { taskMenu.click() }
        cookbookMenuItem.click()

        then:
        at CookbookPage
        waitFor(5) { gebRecipes.size() >= 0 }
    }

    def "Check Cookbook page active elements"() {
        testKey = "TM-XXXX"

        when:
        at CookbookPage

        then:
        taskGenerationTab.parent(".active")
    }

    def "Open Create Recipe page"() {
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

    def "Check Create Recipe page active elements"() {
        testKey = "TM-XXXX"

        when:
        at CreateRecipePage

        then:
        saveButton.@disabled == "true"
        nameFieldContents.@required
        contextSelector2.@required
        brandNewRecipeTab.parent(".active")
    }

    def "Add a Recipe Name"() {
        testKey = "TM-7181"

        given:
        at CreateRecipePage

        when:
        nameFieldContents = "Geb Recipe Test"

        then:
        nameFieldContents == "Geb Recipe Test"
        saveButton.@disabled == "true"
    }

    def "Check Context selector options"() {
        testKey = "TM-7182"

        given:
        at CreateRecipePage

        when:
        contextSelector2.click()
        //interact { moveToElement(contextSelector2) }

        then:
        contextSelector2.$("option")[0].text() == 'Select context'
        contextSelector2.$("option")[1].text() == 'Event'
        contextSelector2.$("option")[2].text() == 'Bundle'
        contextSelector2.$("option")[3].text() == 'Application'
    }

    def "Select an Event context"() {
        testKey = "TM-7183"

        when:
        contextSelector2 = "Event"

        then:
        contextSelector2 == "Event"
    }

    def "Check the Save Button status"() {
        testKey = "TM-XXXX"

        when:
        at CreateRecipePage

        then:
        saveButton.@disabled == ""
    }

    def "Save Recipe"() {
        testKey = "TM-7184"

        when:
        descriptionContents = "This is a Geb created recipe for an Event context"
        saveButton
        saveButton.click()

        then:
        at CookbookPage
    }

/*
// Editor Tab

   def "diff button should be enabled" () {

//    }

// It used to be that when editing a recipe you could click the Diff button and see the changes between the currently edited recipe and WIP or Release. For some reason right now the button is always disabled.
// The button should be enabled when:
  // The user has changed the syntax of recipe
  // user has changed the syntax of a recipe

// The button should be disabled when:
  // Recipe first loaded
  // After Save WIP or Release buttons are clicked successfully

// There are two situations for comparison:
//     When there is a WIP recipe, should compare the local changes to WIP
//     When there is a Version/Release of a recipe and no WIP exists, should compare the local changes to the Version.
*/

    def "Check 'Editor' tab selected after recipe is created"() {
        testKey = "TM-XXXX"

        when:
        at CookbookPage

        then:
        editorTab.parent(".active")
        at TabEditorPage
    }

    def "Check active buttons on 'Editor' tab"() {
        testKey = "TM-XXXX"

        when:
        at TabEditorPage

        then:
        edTabSaveWipBtn.@disabled == "true"
        edTabUndoBtn.@disabled == "true"
        edTabDiffBtn.@disabled == "true"
    }

    def "Open Editor modal window"() {
        testKey = "TM-XXXX"

        given:
        at TabEditorPage

        when:
        edTabEditorBtn.click()

        then:
        at EditRecipePage
 //       editorModalTextArea.text() == " "
    }

    def "Add a recipe form should add text to editor"() {
        testKey = "TM-XXXX"

        given:
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
                '        class: \'application\',',
                '        group: \'BET_THIS_DOES_NOT_EXIST\'',
                '      ],',
                '  ],',
                ']'
        ]
        String recipeText = recipe.join('\\n')

        when:
        browser.driver.executeScript('return angular.element("#recipeModalSourceCode").scope().modal.sourceCode = "'+recipeText+'"');

        then:
//        editorModalTextArea.text() == recipeText
        editorModalTextArea
    }

    def "Close Modal and return to Editor Tab"() {
        testKey = "TM-XXXX"

        when:
        at EditRecipePage

        then:
        editorModalCloseBtn.click()
        at TabEditorPage
    }

    // Save WIP Button

    def "Check Save WIP Button enabled"() {
        testKey = "TM-XXXX"

        when:
        at TabEditorPage

        then:
        edTabSaveWipBtn.@disabled == ""
    }

    def "Save WIP Button should save WIP and should be disabled after"() {
        testKey = "TM-XXXX"

        given:
        at TabEditorPage

        when:
        edTabSaveWipBtn.click()

        then:
//        edTabTextArea == recipeText
        waitFor {edTabSaveWipBtn.@disabled == "true"}
    }

    // Check Syntax button

    def "Check Syntax button should check recipe syntax"() {
        testKey = "TM-XXXX"

        given:
        at TabEditorPage

        when:
        edTabCheckSyntaxBtn.click()

        then:
        TabEditorTabSyntaxErrorsPage
    }

    def "Check Syntax result should have error displayed"() {
        testKey = "TM-XXXX"

        when:
        at TabEditorTabSyntaxErrorsPage

        then:
        SyntErrTabTitle.text()== "Invalid syntax"
        SyntErrTabDetails.text() == 'Task id 1100 \'filter/group\' references an invalid group BET_THIS_DOES_NOT_EXIST'
    }

    // Fix Recipe and check syntax

    def "Edit recipe to fix error"() {
        testKey = "TM-XXXX"

        given:
        at TabEditorPage

        when:
        edTabEditorBtn.click()

        then:
        at EditRecipePage
        //       editorModalTextArea.text() == " "
    }

    def "Add text to editor"() {
        testKey = "TM-XXXX"

        given:
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

        when:
        browser.driver.executeScript('return angular.element("#recipeModalSourceCode").scope().modal.sourceCode = "'+recipeText+'"');

        then:
//        editorModalTextArea.text() == recipeText
        editorModalTextArea
    }

    def "Close button should close Edit Recipe modal window and show recipe on editor tab"() {
        testKey = "TM-XXXX"

        given:
        at EditRecipePage

        when:
        waitFor {editorModalCloseBtn.click()}

        then:
        at TabEditorPage
        //waitFor {editorModalWindow.isDisplayed() == false}
     }

    def "Check Save WIP Button enabled again"() {
        testKey = "TM-XXXX"

        when:
        at TabEditorPage

        then:
        edTabSaveWipBtn.@disabled == ""
    }

    def "Save WIP Button should save WIP and disable the button again"() {
        testKey = "TM-XXXX"

        given:
        at TabEditorPage

        when:
        waitFor {edTabSaveWipBtn.click()}

        then:
//        edTabTextArea == recipeText
        waitFor {edTabSaveWipBtn.@disabled == "true"}
    }

    def "Check Syntax result should have the message 'No errors found' displayed"() {
        testKey = "TM-XXXX"

        given:
        at TabEditorPage

        when:
        waitFor {edTabCheckSyntaxBtn.click()}

        then:
        at TabEditorTabSyntaxErrorsPage
        waitFor {SyntErrTabTitle.text() == "No errors found"}
        waitFor {SyntErrTabDetails.text() == ""}

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

    // Event Dropdown

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
        //interact { moveToElement(contextSelector2) }

        then:
        tskGTabEventSelector.$("option").size() > 1
    }

    def "Set as Default option should be displayed and disabled"() {
        testKey = "TM-XXXX"

        when:
        at TabTaskGenPage

        then:
        tskGTabSetDefaultLink.attr("class")!="ng-hide"
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
        tskGTabEventSelector
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
        tskGTabGenerateTasksBtn.click()

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
        tskGTabSummaryList.find("li", 0).text().contains("Status: Completed")
        tskGTabSummaryList.find("li", 1).text().contains("Tasks Created:")
        tskGTabSummaryList.find("li", 2).text().contains("Number of Exceptions:")

    }
    // Generate Tasks with previously tasks created

    def "should click on generate task button"() {
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

    def "Delete Recipe"() {
        testKey = "TM-7243"

        given:
        at CookbookPage

        when: "Top most Geb Recipe is deleted"
        def gebRecipeCountBeforeDelete = gebRecipes.size()
        println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipeCountBeforeDelete
        //deleteRecipeButtons.each { println it.@title }
        withConfirm(true) { deleteRecipeButtons[0].click() }
        println "${gebReportingSpecTestName.methodName}: Deleting top most recipe."

        then: "Count of geb recipes is down by 1"
        waitFor { gebRecipes.size() == gebRecipeCountBeforeDelete - 1 }
        println "${gebReportingSpecTestName.methodName}: Geb Recipes count = " + gebRecipes.size()
    }

}


