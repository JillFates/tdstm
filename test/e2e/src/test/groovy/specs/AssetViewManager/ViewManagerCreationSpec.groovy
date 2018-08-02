package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.AssetViewManager.AssetViewsPage
import pages.AssetViewManager.SaveViewPage
import pages.AssetViewManager.ViewPage
import utils.CommonActions


@Stepwise
class ViewManagerCreationSpec extends GebReportingSpec {

    def testKey
    static testCount

    //Define the names of the Application you will Create and Edit
    static randStr = CommonActions.getRandomString()
    static viewName=  "QAE2E " +randStr+" View"
    def filteredName=""

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { menuModule.goToAssetViewManager() }
    }
    def "1. Validates user can reach 'my views' "() {
        given: "I am in Asset Views Page"
            at AssetViewsPage
        when: "I click on My Views"
            waitFor {viewMgrMyViews.click() }
        then: "I am taken to My Views page"
            allViewsModule.displayed
    }
    def "2. Validates user can reach Create View Module"() {
        given: "I am in My Views"
            allViewsModule.displayed
        when: "I click on the CREATE button"
            waitFor{allViewsModule.clickCreateView()}
        then: "The Create View Module is displayed"
            createViewModule.displayed
    }
    def "3. Validates user can go back to asset class by Clicking on the tab"() {
        given: "I am in the Create View Module"
            createViewModule.displayed
        when: "I navigate out of Application options tab and I click on the tab again"
            waitFor{createViewModule.selectApplication()}
            waitFor{createViewModule.clickNext()}
            waitFor{createViewModule.goToAssetsClasses()}
        then: "I am taken back back to Application options tab"
            createViewModule.applicationOption.displayed
    }
    def "4. Validate I can go back to Fields tab by Clicking on it"() {
        given: "I am in Application options"
            createViewModule.applicationOption.displayed
        when: "I click on the Fields tab"
            waitFor{createViewModule.goToFields()}
        then: "The application takes me to the Fields tab"
            createViewModule.searchField.displayed
    }
    def "5. Filter Selected Fields and validate only those are displayed"() {
        given: "I am creating a view"
            createViewModule.displayed
        when: "I select random fields (checkboxes) and filter the selected ones"
            createViewModule.clickSpecificCheckbox("Name")
            createViewModule.selectRandomCheckboxes()
            createViewModule.filterFields("Selected")
        then: "Only the selected checkboxes are displayed"
            assert(createViewModule.selectedCheckboxesDisplayed())
    }
    def "6. Validate user can filter only Unselected fields"() {
        given: "User is creating a view"
            createViewModule.fieldsFilter.displayed
        when: "I have filtered the checkboxes by UNSELECTED"
            waitFor{createViewModule.filterFields("Unselected")}
        then: "Only the UNSELECTED checkboxes are visible"
            createViewModule.unselectedCheckboxesDisplayed()
    }
    def "7. Validate user can filter by field name"() {
        given: "The filter by name is present and all fields are diplayed"
            waitFor{createViewModule.searchField.displayed}
            waitFor{createViewModule.filterFields("All Fields")}
        when: "User filters by a random checkbox name"
            filteredName =createViewModule.searchFieldName()
        then: "Only checkboxes containing the text entered in the filter are displayed"
            createViewModule.filteredFieldMatchesDisplay(filteredName)
    }
    def "8. Validate chosen Filters are added below"() {
        given: "User is creating a view"
            createViewModule.fieldsFilter.displayed
            waitFor{createViewModule.clearSearch()}
        when: "User has selected some fields to be displayed"
            waitFor{createViewModule.filterFields("Selected")}
        then: "Column headers for the selected fields are displayed below"
            createViewModule.compareColumns()
    }
    def "9. Click preview and validate data is displayed"() {
        given: "Preview button is present"
            createViewModule.previewBtn.displayed
        when: "I click on the preview button"
            waitFor{createViewModule.clickPreview()}
        then: "Preview data rows are displayed"
            createViewModule.previewDataIsDisplayed()
    }
    def "10. Validate created View is listed"() {
        given: "User saves the created view"
            waitFor{createViewModule.firstSave()}
            at SaveViewPage
            waitFor{enterName(viewName)}
            waitFor{clickSave()}
        when: "User goes to Asset View Manager and filter the views by name"
            waitFor { menuModule.goToAssetViewManager() }
            at AssetViewsPage
            allViewsModule.filterViewByName(randStr)
        then: "The newly created view is filtered and displayed"
            vwGrid.find("tr")[1].find("a")[1].displayed
    }
    def "11. User can go the the newly crated view page"() {
        given: "I have filtered the view"
            println "validate the filtered view is displayed"
        when: "I click on it"
            waitFor{vwGrid.find("tr")[1].find("a")[1].click()}
        then: "I am taken to the view's page"
            at ViewPage
    }
}