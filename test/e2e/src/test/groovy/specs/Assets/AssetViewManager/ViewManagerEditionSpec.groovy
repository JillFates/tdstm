package specs.Assets.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.Assets.AssetViewManager.AssetViewsPage
import pages.Assets.AssetViewManager.SaveViewPage
import pages.Assets.AssetViews.ViewPage
import utils.CommonActions

@Stepwise
class ViewManagerEditionSpec extends GebReportingSpec {

    def testKey
    static testCount
    static partialViewName="QA"
    static selectedView=""
    static expectedColumns =[]

    //Define the names of the Application you will Create and Edit
    static randStr = CommonActions.getRandomString()


    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        assetsModule.goToAssetViewManager()
        at AssetViewsPage
        //Creates a view to use in validations
        allViewsModule.clickCreateView()
        at ViewPage
        createViewModule.selectApplication()
        createViewModule.clickNext()
        createViewModule.clickSpecificCheckbox("Name")
        firstSave()
        at SaveViewPage
        enterName(randStr)
        clickSave()
        at ViewPage
        verifyButtonIsDefaultWithNoChanges()
        clickViewManagerBreadCrumb()
    }

    def "1. validates user can reach'my views' "() {
        given: "I am in Asset Views Page"
            at AssetViewsPage
        when: "I click on My Views"
            waitFor {viewMgrMyViews.click() }
        then: "I am taken to My Views page"
            allViewsModule.displayed
    }

    def "2. validate I can open a View to edit it"(){
        given: "I am in All views section"
            at AssetViewsPage
        when: "I click on a view"
            selectedView=allViewsModule.openRandomView()
        then: "I am taken to the view's page"
            at ViewPage
    }

    def "3. Validate I can go to Fields tab"() {
        given: "I am in View Page"
            at ViewPage
        when: "I click on the gear"
            clickOnGear()
        then: "The application takes me to the Fields tab"
            waitFor{createViewModule.validateFieldsTab()}
    }

    def "4. Filter Selected Fields and validate only those are displayed"() {
        given: "I am editing a view"
            createViewModule.displayed
        when: "I select random fields (checkboxes)"
            createViewModule.selectRandomCheckboxes()
        then: "The selected checkboxes are displayed in the columns below"
            createViewModule.compareColumns()
    }

    def "5. Validate user can filter by field name"() {
        given: "The filter by name is present and all fields are diplayed"
            createViewModule.searchField.displayed
        when: "User filters by a random checkbox name"
            def filteredName =createViewModule.searchFieldName()
        then: "Only checkboxes containing the text entered in the filter are displayed"
            createViewModule.filteredFieldMatchesDisplay(filteredName)
    }

    def "6. Validate user can filter only Unselected fields"() {
        given: "User is creating a view"
            createViewModule.fieldsFilter.displayed
            createViewModule.clearSearch()
        when: "I have filtered the checkboxes by UNSELECTED"
            waitFor{createViewModule.filterFields("Unselected")}
        then: "Only the UNSELECTED checkboxes are visible"
            createViewModule.unselectedCheckboxesDisplayed()
    }

    def "7. Filter Selected Fields and validate only those are displayed"() {
        given: "I am editing a view"
            createViewModule.displayed
        when: "I select random fields (checkboxes) and filter the selected ones"
            createViewModule.selectRandomCheckboxes()
            createViewModule.filterFields("Selected")
        then: "Only the selected checkboxes are displayed"
            createViewModule.selectedCheckboxesDisplayed()
    }

    def "8. Click preview and validate data is displayed"() {
        given: "User is in edit mode"
            createViewModule.previewBtn.displayed
            waitFor{createViewModule.filterFields("All Fields")}
        when: "User clicks preview"
            waitFor{createViewModule.clickPreview()}
        then: "The data is displayed"
            createViewModule.previewDataIsDisplayed()
    }

    def "9. Validate changes were saved"() {
        given: "User has edited the view"
            at ViewPage
            expectedColumns = createViewModule.getListOfSelectedFields()
        when: "User saves the changes made"
            clickSave()
            waitFor { clickViewManagerBreadCrumb() }
            at AssetViewsPage
            waitFor { allViewsModule.openViewByName(selectedView) }
            at ViewPage
        then: "The selected fields are now displayed as columns"
            expectedColumnsDisplayed(expectedColumns)
    }

    def "10. Filter by application name"() {
        given: "User is in edit mode of a view"
            waitFor{clickViewManagerBreadCrumb()}
            at AssetViewsPage
            waitFor{allViewsModule.openViewByName(randStr)}
            at ViewPage
        when: "User filters a name in the grid"
            waitFor{filterPreviewByText(partialViewName)}
        then: "The rows displayed will contain the filtered text"
            validateFilteredRows(partialViewName)
    }
}
