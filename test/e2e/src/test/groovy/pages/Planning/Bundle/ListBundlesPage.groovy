package pages.Planning.Bundle

import geb.Page
import modules.PlanningModule
import org.openqa.selenium.Keys

class ListBundlesPage extends Page {

    static at = {
        listBundlesPageTitle.text() == "Bundle List"
        listBundlesPageBreadcrumbs[0].text()   == "Planning"
        listBundlesPageBreadcrumbs[1].text()   == "Bundles"
        listBundlesPageBreadcrumbs[2].text()   == "List"
    }

    static content = {
        listBundlesPageTitle { $("section", class:"content-header").find("h1")}
        planningModule { module PlanningModule}
        listBundlesPageBreadcrumbs { $("ol", class:"breadcrumb").find("li a")}
        createButton {$("button",class:"action-toolbar-btn")}
        rows {$("[role='rowgroup']")}
        firstBundleListed {$("tbody > tr:nth-child(1)").find("a")}
        tickIcon {$("span.glyphicon-ok")}


        //filters section
        filterRow {$("tr.k-filter-row")}
        namefilterKind {$("span.k-select")[2]}
        nameFilter {filterRow.find("[data-text-field='name']")}
    }

    def clickCreate(){
        createButton.click()
    }

    def filterByName(name){
        nameFilter=name
        //This tab is necessary for the filter will not be applied until the focus is moved away
        //from the field
        nameFilter<< Keys.chord(Keys.TAB)
    }

    def selectFilter(){
        namefilterKind.click()
        $("li.k-item", text:"Contains").click()
    }

    def clickOnBundle(){
        //firstBundleListed.click()
        def bundleLocator = "('tbody.tr')"
        $(".cell-url-element")[0].click()
    }

    /**
     * This filter actually has different options to filter.
     * this method will validate the "equal to" option
     * @author ingrid
     */
    def validateFilteredListEqTo(name){
        $("a.cell-url-element", text:name).displayed
    }

    def validateFilteredDescription(desc){
        rows.find("[role='gridcell']")[2].text()==desc
    }

    def selectByName(name){
        filterByName(name)
        $("a.cell-url-element", text:name).click()
    }
    /**
     * Returns true if description and tick presence are as expected.
     * name has already been validated
     * @param data
     * @return
     */
    def validateBundleRowData(data){
        validateFilteredDescription(data[1])&& validatePlanningTick(data[3])
    }
    /**
     * returns false if the parameter value and the presence of the tick do not match     *
     */
    def validatePlanningTick(value){
        !(value=="on" ^ tickIcon.displayed)
    }

}

