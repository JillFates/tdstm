package specs.Tasks.Cookbook

import geb.spock.GebReportingSpec
import pages.Tasks.Cookbook.CookbookPage
import pages.Tasks.Cookbook.CreateRecipePage
import pages.Tasks.Cookbook.EditRecipePage
import pages.Tasks.Cookbook.TabEditorPage
import pages.Tasks.Cookbook.TabEditorTabSyntaxErrorsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class RecipeEditionSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static recipeName = baseName + " " + randStr + " Geb Recipe Test"
    static recipeDataMap = [
        name: recipeName,
        context: "Event",
        description: "This is a Geb created recipe for an Event context"
    ]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        menuModule.goToTasksCookbook()
        at CookbookPage
        commonsModule.blockCookbookLoadingIndicator() // disable loading for this spec
        clickOnCreateButton()
        at CreateRecipePage
        createRecipe recipeDataMap
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Selecting the Recipe to edit"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Cookbook Section'
            at CookbookPage
        when: 'The User searches by the Recipe'
            waitFor { gebRecipes.getAt(0).click()}

        then: 'Recipe should be shown'
            gebRecipes.getAt(0).text().trim() == recipeName
    }

    def "2. Selecting 'Editor' tab"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Cookbook Section'
            at CookbookPage
        when: 'The User clicks the "Editor" Tab'
            editorTab.click()

        then: 'The Editor Section should be Displayed'
            at TabEditorPage
        and: 'The Editor Tab should be Active'
            waitFor { editorTab.parent(".active") }
    }

    def "3. Checking active buttons on 'Editor' tab"() {
        testKey = "TM-XXXX"
        when: 'The User is on the Editor Section'
            at TabEditorPage

        then: 'Editor Tab should be active'
            edTabSaveWipBtn.@disabled == "true"
            edTabUndoBtn.@disabled == "true"
            edTabDiffBtn.@disabled == "true"
    }

    def "4. Opening the editor modal window"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Editor Section'
            at TabEditorPage
        when: 'The User clicks the Editor Button'
            edTabEditorBtn.click()

        then: 'The User should be in the Editor Recipe Page'
            at EditRecipePage
    }

    def "5. Adding a recipe from the text Editor"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Editor Section'
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
        when: 'The User completes all the Recipe Information'
            browser.driver.executeScript('return angular.element("#recipeModalSourceCode").scope().modal.sourceCode = "'+recipeText+'"');
        then: 'The User should be in the Modal Area'
            editorModalTextArea
    }

    def "6. Closing the Modal and returning  to the 'Editor' Tab"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Edit Recipe Section'
            at EditRecipePage
        when: 'The User clicks the "Close" Button'
            editorModalCloseBtn.click()

        then: 'Editor Page should be displayed'
            at TabEditorPage
    }

    def "7. Checking 'Save WIP' Button"() {
        testKey = "TM-XXXX"
        when:  'The User is on the Edit Recipe Tab Section'
            at TabEditorPage

        then: 'Save WIP Button should be disabled'
            edTabSaveWipBtn.@disabled == ""
    }

    def "8. Save WIP Button status after Saving the WIP"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Edit Recipe Tab Section'
            at TabEditorPage
        when: 'The User clicks the "Save WIP" Option'
            edTabSaveWipBtn.click()

        then: 'The "Save WIP" Button should be disabled'
            //   TODO should compare the recipe text vs textarea
            waitFor {edTabSaveWipBtn.@disabled == "true"}
    }

    def "9. Checking Syntax button"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Edit Recipe Tab Section'
            at TabEditorPage
        when: 'The User clicks the "Check Syntax" Button'
            edTabCheckSyntaxBtn.click()

        then: 'An Error should be displayed'
            TabEditorTabSyntaxErrorsPage
    }

    def "10. Checking Syntax result should display errors"() {
        testKey = "TM-XXXX"
        when: 'The User is on the Syntax Error Section'
            at TabEditorTabSyntaxErrorsPage

        then: 'Invalid Syntax error should be displayed'
            SyntErrTabTitle.text()== "Invalid syntax"
            SyntErrTabDetails.text() == 'Task id 1100 \'filter/group\' references an invalid group BET_THIS_DOES_NOT_EXIST'
    }

    // Fix Recipe and check syntax
    def "11. Editing a Recipe to fixing Errors"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Edit Recipe Tab Section'
            at TabEditorPage
        when: 'The User clicks the "Edit" Button'
            edTabEditorBtn.click()

        then: 'Edit Recipe Section should be Displayed'
            at EditRecipePage
            //   TODO should compare the recipe text vs textarea
    }

    def "12. Adding text to editor"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Edit Recipe Tab Section'
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
        when: 'The User adds all related information'
            browser.driver.executeScript('return angular.element("#recipeModalSourceCode").scope().modal.sourceCode = "'+recipeText+'"');

        then: 'The TextArea should be displayed'
            //   TODO should compare the recipe text vs textarea
            editorModalTextArea
    }

    def "13. Closing button should close Edit Recipe modal window and show recipe on editor tab"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Edit Recipe Section'
            at EditRecipePage
        when: 'The User clicks the "Close" Button'
            waitFor {editorModalCloseBtn.click()}

        then: 'The User should be redirected to the Editor Tab'
            at TabEditorPage
            //   TODO should verify if the modal is closed
    }

    def "14. Checking Save WIP Button is enabled again"() {
        testKey = "TM-XXXX"
        when: 'The User is on the Edit Recipe Section'
            at TabEditorPage

        then: 'The Save WIP button is Enabled'
            edTabSaveWipBtn.@disabled == ""
    }

    def "15. Save WIP Button should save WIP and disable the button again"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Edit Recipe Section'
            at TabEditorPage
        when: 'The User clicks the "Save" WIP Button'
            waitFor {edTabSaveWipBtn.click()}

        then: 'Save WIP Button should be disabled again'
            //   TODO should compare the recipe text vs textarea eg: edTabTextArea == recipeText
            waitFor {edTabSaveWipBtn.@disabled == "true"}
    }

    def "16. Check Syntax result should have the message 'No errors found' displayed"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Edit Recipe Section'
            at TabEditorPage
        when: 'The User clicks the Check Syntax Button'
            waitFor {edTabCheckSyntaxBtn.click()}

        then: 'No errors should be displayed'
            at TabEditorTabSyntaxErrorsPage
            waitFor {SyntErrTabTitle.text() == "No errors found"}
            waitFor {SyntErrTabDetails.text() == ""}
    }
}


