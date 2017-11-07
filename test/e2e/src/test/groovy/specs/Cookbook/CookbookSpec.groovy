package specs.Cookbook

import geb.spock.GebReportingSpec
import javafx.scene.control.Alert
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.EditRecipePage
import pages.Cookbook.ErrorMessagePage
import pages.Cookbook.TabEditorPage
import pages.Cookbook.TabEditorTabSyntaxErrorsPage
import pages.Cookbook.TabHistoryPage
import pages.Cookbook.TabHistoryTabActionsPage
import pages.Cookbook.TabHistoryTabGenLogPage
import pages.Cookbook.TabHistoryTabTasksPage
import pages.Cookbook.TabTaskGenPage
import pages.Cookbook.TabTaskGenTabSummaryPage
import pages.Cookbook.TaskDetailsPage
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
        waitFor(5) { gebRecipes.size() > 0 }
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

    def "Open 'Create Recipe' page"() {
        testKey = "TM-7180"

        given:
        at CookbookPage

        when:
        createRecipeButton.click()

        then:
        at CreateRecipePage
    }

    def "Check 'Create Recipe' page active elements"() {
        testKey = "TM-XXXX"

        when:
        at CreateRecipePage

        then:
        saveButton.@disabled == "true"
        nameFieldContents.@required == "true"
        contextSelector2.@required == "true"
        brandNewRecipeTab.parent(".active")
    }

    def "Add a recipe name"() {
        testKey = "TM-7181"

        given:
        at CreateRecipePage

        when:
        nameFieldContents = "Geb Recipe Test"

        then:
        nameFieldContents == "Geb Recipe Test"
        saveButton.@disabled == "true"
    }

    def "Check 'Context' selector options"() {
        testKey = "TM-7182"

        given:
        at CreateRecipePage

        when:
        contextSelector2.click()

        then:
        contextSelector2.$("option")[0].text() == 'Select context'
        contextSelector2.$("option")[1].text() == 'Event'
        contextSelector2.$("option")[2].text() == 'Bundle'
        contextSelector2.$("option")[3].text() == 'Application'
    }

    def "Select an 'Event' context"() {
        testKey = "TM-7183"

        when:
        contextSelector2 = "Event"

        then:
        contextSelector2 == "Event"
    }

    def "Check the 'Save' Button status"() {
        testKey = "TM-XXXX"

        when:
        at CreateRecipePage

        then:
        saveButton.@disabled == ""
    }

    def "Add description contents"() {
        testKey = "TM-XXXX"

        given:
        at CreateRecipePage

        when:
        descriptionContents = "This is a Geb created recipe for an Event context"

        then:
        saveButton.@disable == ""
    }

    def "Save recipe and check the description"() {
        testKey = "TM-7184"
        def selectedRow = 0

        given:
        at CreateRecipePage

        when:
        saveButton.click()

        then:
        at CookbookPage
        (recipeGridRows[0].find("div", "ng-repeat":"col in renderedColumns"))[1].text().contains("This is a Geb created recipe for an Event context")
        recipeGridRowsCols.getAt(selectedRow*rowSize + 0).text().trim() == "Geb Recipe Test"
        recipeGridRowsCols.getAt(selectedRow*rowSize+1).text().trim() == "This is a Geb created recipe for an Event context"
        recipeGridRowsCols.getAt(selectedRow*rowSize+2).text().trim() == "Event"
        recipeGridRowsCols.getAt(selectedRow*rowSize+3).text().trim() == "e2e user"
        // TODO next line will check dates for the new recipe. Verify actual local time
        // recipeGridRowsCols.getAt(selectedRow*rowSize+4).text().trim() == now()
        recipeGridRowsCols.getAt(selectedRow*rowSize+5).text().trim() == ""
        recipeGridRowsCols.getAt(selectedRow*rowSize+6).text().trim() == "yes"
    }

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

    def "Open editor modal window"() {
        testKey = "TM-XXXX"

        given:
        at TabEditorPage

        when:
        edTabEditorBtn.click()

        then:
        at EditRecipePage
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
        editorModalTextArea
    }

    def "Close Modal and return to 'Editor' Tab"() {
        testKey = "TM-XXXX"

        when:
        at EditRecipePage

        then:
        editorModalCloseBtn.click()
        at TabEditorPage
    }

    // Save WIP Button

    def "Check 'Save WIP' Button enabled"() {
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
//   TODO should compare the recipe text vs textarea
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
//   TODO should compare the recipe text vs textarea
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
//   TODO should compare the recipe text vs textarea
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
//   TODO should verify if the modal is closed
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
//   TODO should compare the recipe text vs textarea eg: edTabTextArea == recipeText
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

    // History Tab (empty)

    def "Go to empty 'History' tab"() {
        testKey = "TM-XXXX"

        given:
        at CookbookPage

        when:
        historyTab.click()

        then:
        at TabHistoryPage
        waitFor { historyTab.parent(".active") }
    }

    def "Check 'History' tab empty elements"() {
        testKey = "TM-XXXX"

        when:
        at TabHistoryPage

        then:
        historyTab.parent(".active")
        hisTabBatchGridHeadCols.getAt(0).find("div",class:"ngHeaderText").text().trim() == "Message"
        hisTabBatchGridRowsCols.getAt(0).find("div",class:"ngCellText").text().trim() == "No results found"
    }

    def "Check Cookbook page title changed"(){
        testKey = "TM-XXXX"

        when:
        at CookbookPage

        then:
        pageTitle.text().trim() == "Generation History"
    }

    def "Go to empty 'Actions' tab"() {
        testKey = "TM-XXXX"

        given:
        at TabHistoryPage

        when:
        hisTabActTab.click()

        then:
        at TabHistoryTabActionsPage
        waitFor { hisTabActTab.parent(".active") }
    }

    def "Check 'Actions' tab blocked elements"() {
        testKey = "TM-XXXX"

        when:
        at TabHistoryTabActionsPage

        then:
        hisTabActTab.parent(".active")
        hisTabActTabPublishBtn.text() == "Publish"
        hisTabActTabPublishBtn.@disabled == "true"
        hisTabActTabResetBtn.@disabled == "true"
        hisTabActTabRefreshBtn.@disabled == "true"
        hisTabActTabDeleteBtn.@disabled == "true"
    }

    def "Go to empty 'Generation Log' tab"() {
        testKey = "TM-XXXX"

        given:
        at TabHistoryPage

        when:
        hisTabGenLogTab.click()

        then:
        at TabHistoryTabGenLogPage
        waitFor { hisTabGenLogTab.parent(".active") }
    }

    def "Check 'Generation Log' tab 'Exception' empty text"() {
        testKey = "TM-XXXX"

        when:
        at TabHistoryTabGenLogPage

        then:
        hisTabGenLogTabExcpRadio == "exceptionLog"
        hisTabGenLogTabTxt.text().trim() == ""
    }

    def "Check 'Generation Log' tab 'Info/Warning' empty text"() {
        testKey = "TM-XXXX"

        given:
        at TabHistoryTabGenLogPage

        when:
        hisTabGenLogTabInfoRadio.click()

        then:
        hisTabGenLogTabInfoRadio == "infoLog"
        hisTabGenLogTabTxt.text().trim() == ""
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

    // History tab wirh non published recipe
    def "Go to History tab with non published recipe"() {
        testKey = "TM-XXXX"

        given:
        at CookbookPage

        when:
        historyTab.click()

        then:
        at TabHistoryPage
        waitFor {historyTab.parent(".active")}
    }

    def "Check History tab active elements"() {
        testKey = "TM-XXXX"

        when:
        at TabHistoryPage

        then:
        waitFor {hisTabActTab.parent(".active")}
        hisTabBatchGridHeadCols.getAt(0).text().trim() == "Context Target"
        hisTabBatchGridHeadCols.getAt(1).text().trim() == "Tasks"
        hisTabBatchGridHeadCols.getAt(2).text().trim() == "Exceptions"
        hisTabBatchGridHeadCols.getAt(3).text().trim() == "Generated By"
        hisTabBatchGridHeadCols.getAt(4).text().trim() == "Generated At"
        hisTabBatchGridHeadCols.getAt(5).text().trim() == "Status"
        hisTabBatchGridHeadCols.getAt(6).text().trim() == "Version"
        hisTabBatchGridHeadCols.getAt(7).text().trim() == "Published"
        hisTabBatchGridHeadCols.getAt(8).text().trim() == "Actions"
    }

    def "Go to Actions tab with actived buttons"() {
        testKey = "TM-XXXX"

        given:
        at TabHistoryPage

        when:
        hisTabActTab.click()

        then:
        at TabHistoryTabActionsPage
    }

    def "Check Actions tab active elements"() {
        testKey = "TM-XXXX"

        when:
        at TabHistoryTabActionsPage

        then:
        hisTabActTabPublishBtn.text() == "Publish"
        hisTabActTabPublishBtn.@disabled == ""
        hisTabActTabResetBtn.@disabled == ""
        hisTabActTabRefreshBtn.@disabled == ""
        hisTabActTabDeleteBtn.@disabled == ""
    }

    def "Go to Tasks tab with recipe tasks values "() {
        testKey = "TM-XXXX"

        given:
        at TabHistoryPage

        when:
        hisTabTasksTab.click()

        then:
        at TabHistoryTabTasksPage
        waitFor(5) {hisTabTasksTabTasksList.size() > 0 }
    }

    def "Check Tasks tab active elements"() {
        testKey = "TM-XXXX"

        when:
        at TabHistoryTabTasksPage

        then:
        waitFor(5) {hisTabTasksTabTasksList.size() > 0 }
        hisTabTasksTabTasksGridHeadCols.getAt(0).text() == "Task #"
        hisTabTasksTabTasksGridHeadCols.getAt(1).text() == "Description"
        hisTabTasksTabTasksGridHeadCols.getAt(2).text() == "Asset"
        hisTabTasksTabTasksGridHeadCols.getAt(3).text() == "Team"
        hisTabTasksTabTasksGridHeadCols.getAt(4).text() == "Person"
        hisTabTasksTabTasksGridHeadCols.getAt(5).text() == "Due date"
        hisTabTasksTabTasksGridHeadCols.getAt(6).text() == "Status"
    }

    def "Select the first task for get its details"() {
        testKey = "TM-XXXX"

        given:
        at TabHistoryTabTasksPage

        when:
        waitFor {hisTabTasksTabTasksList[0].click()}

        then:
        at TaskDetailsPage
    }

    def "Check Tasks Details values"() {
        testKey = "TM-XXXX"
        def taskNumber
        def taskName

        given:
        at TabHistoryTabTasksPage
        taskNumber = hisTabTasksTabTasksFirstRowValues.getAt(0).find("div",class:"ngCellText").text()
        taskName = hisTabTasksTabTasksFirstRowValues.getAt(1).find("div",class:"ngCellText").text()

        when:
        at TaskDetailsPage

        then:
        // TODO Compare the Tasks table value first row vs selected Task Details value
        taskName == taskDetailsTaskName.text().trim()
        taskNumber == taskDetailsNumber.text().trim()
    }

    def "Close 'Tasks Details' modal window"() {
        testKey = "TM-XXXX"

        when:
        at TaskDetailsPage

        then:
        waitFor {taskDetailsModalCloseBtn.click()}
        // TODO check window modal closed
    }

    def "Go to Generation Log tab"() {
        testKey = "TM-XXXX"

        given:
        at TabHistoryPage

        when:
        hisTabGenLogTab.click()

        then:
        at TabHistoryTabGenLogPage
    }

    def "Check Generation Log tab active elements"() {
        testKey = "TM-XXXX"

        when:
        at TabHistoryTabGenLogPage

        then:
        hisTabGenLogTabExcpRadio == "exceptionLog"
        hisTabGenLogTabTxt.text().contains("has no predecessor tasks")
    }

    def "Click on Info/Warning radio"() {
        testKey = "TM-XXXX"

        given:
        at TabHistoryTabGenLogPage

        when:
        hisTabGenLogTabInfoRadio.click()

        then:
        hisTabGenLogTabInfoRadio == "infoLog"
        hisTabGenLogTabTxt.text().contains("A total of")
        hisTabGenLogTabTxt.text().contains("Tasks and")
        hisTabGenLogTabTxt.text().contains("Dependencies created in")
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


