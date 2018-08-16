package specs.Cookbook

import geb.spock.GebReportingSpec
import pages.Cookbook.CookbookPage
import pages.Cookbook.CreateRecipePage
import pages.Cookbook.EditRecipePage
import pages.Cookbook.TabTaskGenPage
import pages.Cookbook.TabEditorPage
import pages.Cookbook.TabHistoryPage
import pages.Cookbook.TabHistoryTabActionsPage
import pages.Cookbook.TabHistoryTabGenLogPage
import pages.Cookbook.TabHistoryTabTasksPage
import pages.Cookbook.TabTaskGenTabSummaryPage
import pages.Cookbook.TaskDetailsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions


@Stepwise
class RecipeHistorySpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static recipeName = baseName + " " + randStr + " Recipe"
    static recipeWithTasksName = baseName + " " + randStr + " Recipe with Tasks"
    static recipeDataMap = [
            name: recipeName,
            description: "This is a Geb created recipe"
    ]

    static recipeWithTasksDataMap = [
            name: recipeWithTasksName,
            description: "This is a Geb created recipe",
            recipeText: [
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
    ]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        menuModule.goToTasksCookbook()
        at CookbookPage
        commonsModule.blockCookbookLoadingIndicator() // disable loading for this spec
        // Create clean recipe verify stuff
        clickOnCreateButton()
        at CreateRecipePage
        createRecipe recipeDataMap
        // Create recipe with tasks verify stuff
        at CookbookPage
        clickOnCreateButton()
        at CreateRecipePage
        createRecipe recipeWithTasksDataMap
        at CookbookPage
        /* EDIT Recipe */
        openEditTab()
        at TabEditorPage
        clickOnEditButton()
        at EditRecipePage
        browser.driver.executeScript('return angular.element("#recipeModalSourceCode").scope().modal.sourceCode = "'+recipeWithTasksDataMap.recipeText+'"');
        waitFor {editorModalCloseBtn.click()}
        at TabEditorPage
        waitFor {edTabSaveWipBtn.click()}
        at CookbookPage
        waitFor {taskGenerationTab.click()}
        at TabTaskGenPage
        def buildOutOp = tskGTabEventSelector.find("option").find{it.text() == "Buildout"}
        waitFor { buildOutOp.click()}
        waitFor { tskGTabGenUsingWipCBox.click()}
        waitFor { tskGTabGenerateTasksBtn.click()}
        waitForProgressBar()
        at TabTaskGenTabSummaryPage
        waitFor { tskGTabSummaryList.find("li", 0).text().contains("Status: Complete")}
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Selecting the Empty Recipe to verify its History"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Cookbook Section'
            at CookbookPage
        when: 'The User clicks in the Recipe'
            waitFor { getRecipeByName(recipeDataMap.name).click()}
        then: 'Text containing the Recipe Name should be displayed'
            waitFor { getRecipeNameDisplayedInTaskGenerationTab().contains(recipeDataMap.name)} }

    // History Tab (empty)

    def "2. Verifying the Information in the 'History' tab"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Cookbook Section'
            at CookbookPage
        when: 'The User clicks the History Option'
            waitFor { historyTab.click()}

        then: 'History Tab should be Active'
            at TabHistoryPage
            waitFor { historyTab.parent(".active") }
    }

    def "3. Check Empty Elements in the 'History' tab"() {
        testKey = "TM-XXXX"
        when: 'The User is in the History Section'
            at TabHistoryPage

        then: 'Empty Values should be displayed'
            historyTab.parent(".active")
            hisTabBatchGridHeadCols.getAt(0).find("div",class:"ngHeaderText").text().trim() == "Message"
            hisTabBatchGridRowsCols.getAt(0).find("div",class:"ngCellText").text().trim() == "No results found"
    }

    def "4. Checking The Cookbook page title"(){
        testKey = "TM-XXXX"
        when: 'The User is in the Cookbook Section'
            at CookbookPage

        then: 'The Page title should reflect the Generation History Legend'
            pageTitle.text().trim() == "Generation History"
    }

    def "5. Going to the empty 'Actions' tab"() {
        testKey = "TM-XXXX"
        given: 'The User is in the History Section'
            at TabHistoryPage
        when: 'The User clicks on that Option'
            hisTabActTab.click()

        then: 'History Tab should be Active'
            at TabHistoryTabActionsPage
            waitFor { hisTabActTab.parent(".active") }
    }

    def "6. Checking blocked elements on The 'Actions' tab"() {
        testKey = "TM-XXXX"
        when: 'The User is in the History Section'
            at TabHistoryTabActionsPage

        then: 'Some Elements should be disabled'
            hisTabActTab.parent(".active")
            hisTabActTabPublishBtn.text() == "Publish"
            hisTabActTabPublishBtn.@disabled == "true"
            hisTabActTabResetBtn.@disabled == "true"
            hisTabActTabRefreshBtn.@disabled == "true"
            hisTabActTabDeleteBtn.@disabled == "true"
    }

    def "7. Going to the empty 'Generation Log' tab"() {
        testKey = "TM-XXXX"
        given: 'The User is in the History Section'
            at TabHistoryPage
        when: 'The User Clicks the Generation Log Tab'
            hisTabGenLogTab.click()

        then: 'The User should be redirected to the Generation Log Section'
            at TabHistoryTabGenLogPage
            waitFor { hisTabGenLogTab.parent(".active") }
    }

    def "8, Checking 'Generation Log' tab 'Exception' empty text"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Generation Log Section'
            at TabHistoryTabGenLogPage

        then: 'Text should not be present'
            waitFor { hisTabGenLogTabExcpRadio == "exceptionLog"}
            waitFor { hisTabGenLogTabTxt.text().trim() == ""}
    }

    def "9. Checking 'Generation Log' tab 'Info/Warning' empty text"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Generation Log Section'
            at TabHistoryTabGenLogPage
        when: 'The User clicks the Info Radio Option'
            hisTabGenLogTabInfoRadio.click()

        then: 'Text should not be present'
            hisTabGenLogTabInfoRadio == "infoLog"
            hisTabGenLogTabTxt.text().trim() == ""
    }

    def "10. Selecting the Recipe with generated tasks to verify History"() {
        testKey = "TM-XXXX"
        given: 'The User is in the CookBook Section'
            at CookbookPage
        when: 'The User searches by a Recipe'
            waitFor { getRecipeByName(recipeWithTasksDataMap.name).click()}
        and: 'The user goes to Task generation tab'
            waitFor {taskGenerationTab.click()}
        then: 'Text containing the Recipe Name should be displayed'
            waitFor { getRecipeNameDisplayedInTaskGenerationTab().contains(recipeWithTasksDataMap.name)}
    }

    def "11. Going to the populated 'History' tab"() {
        testKey = "TM-XXXX"
        given: 'The User is in the CookBook Section'
            at CookbookPage
        when: 'The User Clicks the History Tab Option'
            historyTab.click()

        then: 'The User should be redirected to that Section'
            at TabHistoryPage
            waitFor { historyTab.parent(".active") }
    }

    def "12. Checking the History tab active elements"() {
        testKey = "TM-XXXX"
        when: 'The User is in the History Tab Section'
            at TabHistoryPage

        then: 'Different Elements should be active'
            hisTabBatchGridHeadCols.getAt(0).text().trim() == "Event"
            hisTabBatchGridHeadCols.getAt(1).text().trim() == "Tags"
            hisTabBatchGridHeadCols.getAt(2).text().trim() == "Tasks"
            hisTabBatchGridHeadCols.getAt(3).text().trim() == "Exceptions"
            hisTabBatchGridHeadCols.getAt(4).text().trim() == "Generated By"
            hisTabBatchGridHeadCols.getAt(5).text().trim() == "Generated At"
            hisTabBatchGridHeadCols.getAt(6).text().trim() == "Status"
            hisTabBatchGridHeadCols.getAt(7).text().trim() == "Version"
            hisTabBatchGridHeadCols.getAt(8).text().trim() == "Published"
            hisTabBatchGridHeadCols.getAt(9).text().trim() == "Actions"
    }

    def "13. Going to Actions tab with actived buttons"() {
        testKey = "TM-XXXX"
        given: 'The User is in the History Tab Section'
            at TabHistoryPage
        when: 'The User Clicks the Tab'
            hisTabActTab.click()

        then: 'The User should be redirected to the History Actions Tab Section'
            at TabHistoryTabActionsPage
    }

    def "14. Checking Actions tab active elements"() {
        testKey = "TM-XXXX"
        when: 'The User is in the History Tab Section'
            at TabHistoryTabActionsPage

        then: 'Different elements should be present'
            hisTabActTabPublishBtn.text() == "Publish"
            hisTabActTabPublishBtn.@disabled == ""
            hisTabActTabResetBtn.@disabled == ""
            hisTabActTabRefreshBtn.@disabled == ""
            hisTabActTabDeleteBtn.@disabled == ""
    }

    def "15. Going to Tasks tab with recipe tasks values "() {
        testKey = "TM-XXXX"
        given: 'The User is in the History Tab Section'
            at TabHistoryPage
        when: 'The User Clicks the Tab'
            hisTabTasksTab.click()

        then: 'History Tab Tasks Values should be present'
            at TabHistoryTabTasksPage
            waitFor {hisTabTasksTabTasksList.size() > 0 }
    }

    def "16. Checking Tasks tab active elements"() {
        testKey = "TM-XXXX"
        when: 'The User is in the History Tab Section'
            at TabHistoryTabTasksPage

        then: 'Active Elements should be shown'
            waitFor {hisTabTasksTabTasksList.size() > 1 }
            hisTabTasksTabTasksGridHeadCols.getAt(0).text() == "Task #"
            hisTabTasksTabTasksGridHeadCols.getAt(1).text() == "Description"
            hisTabTasksTabTasksGridHeadCols.getAt(2).text() == "Asset"
            hisTabTasksTabTasksGridHeadCols.getAt(3).text() == "Team"
            hisTabTasksTabTasksGridHeadCols.getAt(4).text() == "Person"
            hisTabTasksTabTasksGridHeadCols.getAt(5).text() == "Due date"
            hisTabTasksTabTasksGridHeadCols.getAt(6).text() == "Status"
    }

    def "17. Selecting the first task for getting its details"() {
        testKey = "TM-XXXX"
        given: 'The User is in the History Tab Section'
            at TabHistoryTabTasksPage
        when: 'The User clicks on it'
            waitFor {hisTabTasksTabTasksList[0].click()}

        then: 'The User should be redirected to the Task Details Section'
            at TaskDetailsPage
    }
    def "18. Closing 'Tasks Details' modal window"() {
        testKey = "TM-XXXX"
        given: 'The User is inthe Task Details Section'
            at TaskDetailsPage
        when: 'The User clicks the "Close" Option'
            taskDetailsModalCloseBtn.click()

        then: 'The User should be redirected to the Cookbook Section'
            at CookbookPage
            waitFor {!taskDetailsModal.present}
            // TODO check window modal closed
    }

    def "19. Going to the Generation Log tab"() {
        testKey = "TM-XXXX"
        given: 'The User is in the History Tab Section'
            at TabHistoryPage
        when: 'The User clicks the Generation Log Tab'
            hisTabGenLogTab.click()

        then: 'The User should be redirected to the Generation Log Tab'
            at TabHistoryTabGenLogPage
    }

    def "20. Checking Generation Log tab active elements"() {
        testKey = "TM-XXXX"
        when: 'The User is in the Generation Log Tab Section'
            at TabHistoryTabGenLogPage

        then: 'Some values should be present'
            waitFor { hisTabGenLogTabExcpRadio == "exceptionLog"}
            waitFor { hisTabGenLogTabTxt.text().contains("has no predecessor tasks")}
    }

    def "21. Clicking on Info/Warning radio"() {
        testKey = "TM-XXXX"
        given: 'The User is in the Generation Log Tab Section'
            at TabHistoryTabGenLogPage
        when: 'The User Clicks the Info Checkbox'
            hisTabGenLogTabInfoRadio.click()

        then: 'Related information should be displayed'
            hisTabGenLogTabInfoRadio == "infoLog"
            hisTabGenLogTabTxt.text().contains("A total of")
            hisTabGenLogTabTxt.text().contains("Tasks and")
            hisTabGenLogTabTxt.text().contains("Dependencies created in")
    }
}