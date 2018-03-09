package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.AssetViewManager.AssetViewsPage
import pages.AssetViewManager.SaveViewPage
import pages.AssetViewManager.ViewPage



@Stepwise
class AssetViewCreationSpec extends GebReportingSpec {

    def testKey
    static testCount
    static viewName="8500 View"
    static partialName="8500"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { menuModule.goToAssetViewManager() }
        println(">>> next is clicking on my views")
    }

    //validates I can reaach"my views"
    def "Go To myViews"() {
        testKey = "TM-8500"
        given:
        at AssetViewsPage // need to create asset view page <<<<
        when:
        waitFor {viewMgrMyViews.click() }//voy a my views
        then:
        viewsModule.displayed
    }
    /*validates I reach create view Module*/
    def "Go To Create View"() {
        testKey = "TM-8500"
        given:
        viewsModule.displayed
        when:
        waitFor{createViewBton.click()}
        then:
        createViewModule.displayed
    }
    //Validate I can go back to asset class by Cliking on the tab
    def "Go back to Asset Class tab"() {
        testKey = "TM-8500"
        given:
        createViewModule.displayed
        when:
        waitFor{createViewModule.selectApplication()}
        waitFor{createViewModule.clickNext()}
        waitFor{createViewModule.goToAssetsClasses()}
        then:
        createViewModule.applicationOption.displayed
    }
    //Validate I can go back to Fields tab by Clicking on it
    def "Go back to Fields tab"() {
        testKey = "TM-8500"
        given:
        createViewModule.applicationOption.displayed
        when:
        waitFor{createViewModule.goToFields()}
        then:
        createViewModule.searchField.displayed
    }
    //Validate I can choose fields and they re added below
    //TODO: randomly choose the checkboxes
    def "Chosen Filters are added below"() {
        testKey = "TM-8500"
        given:
        createViewModule.displayed
        when:
        waitFor{createViewModule.selectCheckBoxes()}
        then:
        previewGrid.find("div",class:"k-grid-header-wrap").find("label")[0].text()=="Asset Class"

    }
    //TODO: create a collection of the filtered checkboxes and validate their label´s text includes the text entered
    //Validate I can filter by field names
    def "Filter Field names"() {
        testKey = "TM-8500"
        given:
        createViewModule.searchField.displayed
        when:
        waitFor{createViewModule.searchFieldName()}

        then:
        //crtViewPageWindow.find()
        println(">>>> I need to Validate her that only that field is displayed")
    }
    //TODO: create a collection of cehckboxes filtered and validate they are all TRUE random selection of checkboxes
    //Validate I can filter only selected fields
    def "Filter Selected Fields"() {
        testKey = "TM-8500"
        given:
        createViewModule.fieldsFilter.displayed
        waitFor{createViewModule.clearSearch()}
        when:
        waitFor{createViewModule.filterFields("Selected")}
        then:
        println("Need to validate here the only SELECTED fields are displayed")
    }
    //TODO: create a collection of cehckboxes filtered and validate they are all FALSE
    //Validate I can filter only selected fields
    def "Filter Unselected Fields"() {
        testKey = "TM-8500"
        given:
        createViewModule.fieldsFilter.displayed
        when:
        waitFor{createViewModule.filterFields("Unselected")}
        then:
        println("Need to validate here the only UNSELECTED fields are displayed")
    }
    //will click on preview and validate there are data rows displayed
    def "Click preview and validate data is dislayed"() {
        testKey = "TM-8500"
        given:
        createViewModule.previewBtn.displayed
        when:
        waitFor{createViewModule.clickPreview()}
        then:
        println("Need to validate ROWS ARE DISPLAYED")
    }
    //saves the created view and validates it´s listed
    def "Created View Save and Validate"() {
        testKey = "TM-8500"
        given:
            println(" I SAVE THE VIEW")
            waitFor{createViewModule.clickSave()}
            println("clicked save 1")
            at SaveViewPage
            waitFor{enterName(viewName)}
            waitFor{clickSave()}

        when:
            waitFor { menuModule.goToAssetViewManager() }
            at AssetViewsPage
            filterViewByName(partialName)
            waitFor{vwGrid.find("tr")[1].find("a")[1].click()}
        then:
            at ViewPage

    }

}