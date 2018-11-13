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
        individualRows {$("tbody>tr")}
        individualBundles (required: false) {$("a.cell-url-element")}
        commonsModule {module CommonsModule}
        firstBundleListed {$("tbody > tr:nth-child(1)").find("a")}
        tickIcon {$("span.glyphicon-ok")}
        pagerInfo {$(".k-pager-info")}

        //filters section
        filterRow {$("tr.k-filter-row")}
        nameFilterKind {$("span.k-select")[2]}
        descFilterKind {$("span.k-select")[3]}
        nameFilter {filterRow.find("[data-text-field='name']")}
        descriptionFilter {filterRow.find("[data-text-field='description']")}
        planningFilterWrapper {$(".k-operator-hidden")}
        isPlanningRadio {filterRow.find(("label>input"))[0]}
        isNonPlanningRadio {filterRow.find(("label>input"))[1]}
        clearPlanningFilter {planningFilterWrapper.find("button.k-button", title:"Clear")}
        nameFilterWrapper {$("span.k-filtercell")[1]}
        clearName {nameFilterWrapper.find("button.k-button")}
        descFilterWrapper {$("span.k-filtercell")[2]}
        clearDesc {descFilterWrapper.find("button.k-button")}
        clearAssetQtty {filterRow.find("[title='Clear']")[4]}
        clearStarting {filterRow.find("[title='Clear']")[5]}
        clearCompletion {filterRow.find("[title='Clear']")[6]}
        qttyFilter {filterRow.find("input.k-formatted-value.k-input")[1]}
        qttyFilterFocused{filterRow.find("[data-role='numerictextbox']")[1]}
        editedQty {filterRow.find("span.k-state-focused")}
        startDate {$("[data-role='datepicker']")[0]}
        completionDate {$("[data-role='datepicker']")[1]}
    }

    def filterByQuantity(qtty){
        qttyFilter.click()
        qttyFilterFocused=qtty
        //This is necessary for the filter will not be applied until the focus is moved away
        //from the field. Tabbing does not work in this case, so just clicking elsewhere
        nameFilter.click()
    }

    /**
     * This method will validate that bundles are correctly filtered by either checking
     * on the presence or absence of "is Planned" ticks in each row. The options parameter
     * will define which of the two possibilities to check for.
     * @param option
     * @return
     */

    def validateFilteredByPlanning(option){
        def validation=true
        for (int i=1;i<numberOfRows();i++){
            if(option){
                if(!individualRows[i].find("span.glyphicon-ok").displayed) {
                    validation=false
                    break;
                }
            }else{
                if(individualRows[i].find("span.glyphicon-ok").displayed){
                    validation=false
                    break;
                }
            }
        }
        return validation
    }

    /**
     * this method validates that all of the bundle names in the list contain a specified string
     * passed as parameter
     */
    def validateFilteredByName(name){
        def validation=true
        individualBundles.each{
            if(!it.text().contains("QAE2E")){
                validation=false
            }
        }
        return validation
    }

    def validateFilteredByDesc(name){
        def validation=true
        for (int i=1;i<numberOfRows();i++){
            if(!individualRows[i].find("td")[2].text().contains("QAE2E")){
                validation=false
            }
        }
        return validation
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

    def clearNameFilter(){
        clearName.click()
    }

    def clearDescription(){
        clearDesc.click()
    }

    def clearAssetQtty(){
        clearAssetQtty.click()
    }

    def clearStartingDate(){
        clearStarting.click()
    }

    def clearCompletionDate(){
        clearCompletion.click()
    }

    def clickCreate(){
        createButton.click()
    }

    def numberOfRows(){
        individualBundles.size()
    }

    def filterByName(name){
        nameFilter=name
        //This tab is necessary for the filter will not be applied until the focus is moved away
        //from the field
        nameFilter<< Keys.chord(Keys.TAB)
    }

    /**
     * Filters bundles by description
     * @param name
     * @return
     */

    def filterByDesc(name){
        descriptionFilter=name
        //This tab is necessary for the filter will not be applied until the focus is moved away
        //from the field
        descriptionFilter<< Keys.chord(Keys.TAB)
    }

    def selectFilter(){
        nameFilterKind.click()
        waitFor{$("li.k-item", text:"Contains").click()}
    }

    /**
     * selecta the filter type for description
     * @return
     */

    def selectDescFilter(){
        descFilterKind.click()
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

    /**
     * This method validates that each row displayed will contain the expected Asset
     * Quantity
     */

    def validateAssetQtyFilter(qty){
        def validation=true
        for (int i=1;i<numberOfRows();i++){
            if(!rows[i].find(("[role='gridcell']")[3]).contains(qty)) {
                validation=false
                break;
            }
        }
        return validation
    }

    /**
     *  filters by start date if parameter is true, will filter by
     * completion date otherwise
     * @param dt
     * @param isStart
     * @return
     */

    def filterByDate(dt,isStart){
        if(isStart){
            startDate= dt
            startDate << Keys.chord(Keys.TAB)
            return dt
        }else{
            completionDate = dt
            completionDate << Keys.chord(Keys.TAB)
            return dt
        }
    }

    def validateStartDate(dte){
        def validation=true
        for (int i=1;i<numberOfRows();i++){
            if(!rows[i].find(("[role='gridcell']")[5]).contains(dte)) {
                validation=false
                break;
            }
        }
        return validation
    }

    def validateCompletionDate(dte){
        def validation=true
        for (int i=1;i<numberOfRows();i++){
            if(!rows[i].find(("[role='gridcell']")[6]).contains(dte)) {
                validation=false
                break;
            }
        }
        return validation
    }

    def validatePagerInfo(txt){
        pagerInfo.text()==txt
    }

}

