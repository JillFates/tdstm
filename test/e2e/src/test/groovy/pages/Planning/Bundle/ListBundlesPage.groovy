package pages.Planning.Bundle

import geb.Page
import modules.PlanningModule
import modules.CommonsModule
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
        individualRows (required: false) {$("a.cell-url-element")}
        commonsModule {module CommonsModule}
        firstBundleListed {$("tbody > tr:nth-child(1)").find("a")}
        tickIcon {$("span.glyphicon-ok")}

        //filters section
        filterRow {$("tr.k-filter-row")}
        namefilterKind {$("span.k-select")[2]}
        nameFilter {filterRow.find("[data-text-field='name']")}
        planningFilterWrapper {$(".k-operator-hidden")}
        isPlanningRadio {filterRow.find(("label>input"))[0]}
        isNonPlanningRadio {filterRow.find(("label>input"))[1]}
        clearPlanningFilter {planningFilterWrapper.find("button.k-button", title:"Clear")}
    }

    def validateBundleIsListed(bName){
        filterByName(bName)
        numberOfRows()==1
    }

    def verifyRowsDisplayed(){
        selectFilter()
        numberOfRows()>0
    }

    def clickNonPlanningFilter(){
       isNonPlanningRadio.click()
    }

    def clickPlanningFilter(){
        isPlanningRadio.click()
    }

    def clearPlanningFilter(){
        clearPlanningFilter.click()
    }

    def clickCreate(){
        createButton.click()
    }

    def numberOfRows(){
        individualRows.size()
    }

    def filterByName(name){
        nameFilter=name
        //This tab is necessary for the filter will not be applied until the focus is moved away
        //from the field
        nameFilter<< Keys.chord(Keys.TAB)
    }

    def selectFilter(){
        namefilterKind.click()
        waitFor{$("li.k-item", text:"Contains").click()}
    }

    def clickOnBundle(){
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
        if(value=="on"){
            return commonsModule.verifyElementDisplayed($("span.glyphicon-ok"))
        } else {
            return !commonsModule.verifyElementDisplayed($("span.glyphicon-ok"))
        }
    }

}

