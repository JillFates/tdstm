package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.AssetViewManager.AssetViewsPage
import pages.AssetViewManager.SaveViewPage
import pages.AssetViewManager.ViewPage
import jodd.util.RandomString


@Stepwise
class AssetViewCreationSpec extends GebReportingSpec {

    def testKey
    static testCount

    //Define the names of the Application you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "TM8500"
    static viewName=  randStr+" "+baseName
    def filteredName=""

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { menuModule.goToAssetViewManager() }
    }
    //validates I can reach"my views"
    def "Go To myViews"() {
        testKey = "TM-8500"
        given: "I am in Asset Views Page"
            at AssetViewsPage
        when: "I click on My Views"
            waitFor {viewMgrMyViews.click() }
        then: "I am taken to My Views page"
            viewsModule.displayed
    }
    /*validates I reach create view Module*/
    def "Go To Create View"() {
        testKey = "TM-8500"
        given: "I am in My Views"
            viewsModule.displayed
        when: "I click on the CREATE button"
            waitFor{viewsModule.clickCreateView()}
        then: "The Create View Module is displayed"
            createViewModule.displayed
    }
    //Validates I can go back to asset class by Clicking on the tab
    def "Go back to Asset Class tab"() {
        testKey = "TM-8500"
        given: "I am in the Create View Module"
            createViewModule.displayed
        when: "I navigate out of Application options tab and I click on the tab again"
            waitFor{createViewModule.selectApplication()}
            waitFor{createViewModule.clickNext()}
            waitFor{createViewModule.goToAssetsClasses()}
        then: "I am taken back back to Application options tab"
            createViewModule.applicationOption.displayed
    }
    //Validate I can go back to Fields tab by Clicking on it
    def "Go back to Fields tab"() {
        testKey = "TM-8500"
        given: "I am in Application options"
            createViewModule.applicationOption.displayed
        when: "I click on the Fields tab"
            waitFor{createViewModule.goToFields()}
        then: "The application takes me to the Fields tab"
            createViewModule.searchField.displayed
    }
    //filters the selected fields and validates they are displayed.
    def "Filter Selected Fields"() {
        testKey = "TM-8500"
        given: "I am creating a view"
            createViewModule.displayed
        when: "I select random fields (checkboxes) and filter the selected ones"
            createViewModule.selectRandomCheckboxes()
            createViewModule.filterFields("Selected")
        then: "Only the selected checkboxes are displayed"
            assert(createViewModule.selectedCheckboxesDisplayed())
    }
    //Validate I can filter only unselected fields
    def "Filter Unselected Fields"() {
        testKey = "TM-8500"
        given: "I am creating a view"
            createViewModule.fieldsFilter.displayed
        when: "I have filtered the checkboxes by UNSELECTED"
            waitFor{createViewModule.filterFields("Unselected")}
        then: "Only the UNSELECTED checkboxes are visible"
            createViewModule.unselectedCheckboxesDisplayed()
    }
    //Validate I can filter by field names
    def "Filter Field names"() {
        testKey = "TM-8500"
        given: "The filter by name is present and all fields are diplayed"
            waitFor{createViewModule.searchField.displayed}
            waitFor{createViewModule.filterFields("All Fields")}
        when: "I filter by a random checkbox name"
            filteredName =createViewModule.searchFieldName()
        then: "Only checkboxes containing the text entered in the filter are displayed"
            createViewModule.filteredFieldMatchesDisplay(filteredName)
    }
    //Validate I can choose fields and they are added below
    def "Chosen Filters are added below"() {
        testKey = "TM-8500"
        given: "I am creating a view"
            createViewModule.fieldsFilter.displayed
            waitFor{createViewModule.clearSearch()}
        when: "I have selected some fields to be displayed"
            waitFor{createViewModule.filterFields("Selected")}
        then: "Column headers for the selected fields are displayed below"
            createViewModule.compareColumns()
    }
    //will click on preview and validate there are data rows displayed
    def "Click preview and validate data is dislayed"() {
        testKey = "TM-8500"
        given: "Preview button is present"
            createViewModule.previewBtn.displayed
        when: "I click on the preview button"
            waitFor{createViewModule.clickPreview()}
        then: "Preview data rows are displayed"
            createViewModule.previewDataIsDisplayed()
    }
    //saves the created view and validates it´s listed
    def "Created View is listed"() {
        testKey = "TM-8500"
        given: "I save the created view"
            waitFor{createViewModule.clickSave()}
            at SaveViewPage
            waitFor{enterName(viewName)}
            waitFor{clickSave()}
        when: "I go to Asset View Manager and filter the views by name"
            waitFor { menuModule.goToAssetViewManager() }
            at AssetViewsPage
            filterViewByName(randStr)
        then: "The newly created view is filtered and displayed"
            vwGrid.find("tr")[1].find("a")[1].displayed
    }

    def "User can go the the newly crated view page"() {
        testKey = "TM-8500"
        given: "I have filtered the view"
            println "validate the filtered view is displayed"
        when: "I click on it"
            waitFor{vwGrid.find("tr")[1].find("a")[1].click()}
        then: "I am taken to the view's page"
        at ViewPage
    }
}