package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.EditRecipePage
import pages.Cookbook.TabEditorPage
import pages.Cookbook.TabEditorTabSyntaxErrorsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class RecipeEditionSpec extends GebReportingSpec {
    def testKey
    static testCount
    static recipeText

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        menuModule.goToTasksCookbook()
        at CookbookPage
        waitFor { recipeGridRows.size() > 0 }
        waitFor { createRecipeButton.click()}
        at CreateRecipePage
        nameFieldContents = "Geb Recipe Test"
        nameFieldContents == "Geb Recipe Test"
        contextSelector2 = "Event"
        descriptionContents = "This is a Geb created recipe for an Event context"
        saveButton.click()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "Select the Recipe to edit"() {
        testKey = "TM-XXXX"
        given:
        at CookbookPage
        when:
        waitFor { gebRecipes.getAt(0).click()}
        then:
        gebRecipes.getAt(0).text().trim() == "Geb Recipe Test"
    }

    def "Select 'Editor' tab"() {
        testKey = "TM-XXXX"
        given:
        at CookbookPage
        when:
        editorTab.click()
        then:
        at TabEditorPage
        waitFor { editorTab.parent(".active") }
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
}


