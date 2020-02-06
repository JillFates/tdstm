package pages.Planning.Bundle

import geb.Page
import modules.PlanningMenuModule
import modules.CommonsModule
import org.openqa.selenium.Keys

class ListBundlesPage extends Page {

    static at = {
        listBundlesPageTitle.text() == "Bundle List"
        listBundlesPageBreadcrumbs[0].text()   == "Planning"
        listBundlesPageBreadcrumbs[1].text()   == "Bundle List"

    }

    static content = {
        listBundlesPageTitle { $("section", class:"content-header").find("h2")}
        planningModule { module PlanningMenuModule}
        listBundlesPageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        createButton {$("button",class:"action-toolbar-btn")}
        rows (required:false) {$('tdoby',role="presentation")}
        individualRows (required:false) {$('table.k-grid-table').find('tr',role:'row')}
        individualBundles (required: false) {$("a.cell-url-element")}
        commonsModule {module CommonsModule}
        firstBundleListed {$("tbody > tr:nth-child(1)").find("a")}
        tickIcon {$("span.glyphicon-ok")}
        pagerInfo {$(".k-grid-norecords")}

        //filters section
        filterRow {$("tr.k-filter-row")}
        nameFilterKind {$("span.k-select")[2]}
        descFilterKind {$("span.k-select")[3]}
        nameFilter {filterRow.find("[placeholder='Filter Name']")}
        descriptionFilter {filterRow.find("[placeholder='Filter Description']")}
        planningFilterWrapper {$(".k-operator-hidden")}
        isPlanningOptionsContainer (required:false) {$("kendo-popup")}
        planSelect (required:false)  {$("span", role:"listbox")}
        isPlanningSelector {filterRow.find("kendo-dropdownlist")}
        clearPlanningFilter {$("span.form-control-feedback")[1]}
        nameFilterWrapper {$("span.k-filtercell")[1]}
        clearName {$("span.form-control-feedback")[0]}
        descFilterWrapper {$("span.k-filtercell")[2]}
        clearDesc {$("span.form-control-feedback")[0]}
        clearAssetQtty {filterRow.find("span.form-control-feedback")[0]}
        clearStarting {filterRow.find("[title='Clear']")[5]}
        clearCompletion {filterRow.find("[title='Clear']")[6]}
        qttyFilter {filterRow.find("input.k-input", placeholder:"Filter Asset Quantity")}
        qttyFilterFocused{filterRow.find("[data-role='numerictextbox']")[1]}
        editedQty {filterRow.find("span.k-state-focused")}
        startDate {$('input.k-input')[1]}
        completionDate {$('input.k-input')[2]}
        noRecordsMsg (required:false) {$('tr.k-grid-norecords')}
    }

    def filterByQuantity(qtty){
        qttyFilter.click()
        qttyFilter = qtty

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
            if(!individualRows[i].find("td")[1].text().contains("QAE2E")){
                validation=false
            }
        }
        return validation
    }

    def validateBundleIsListed(bName){
        filterByName(bName)
        sleep(1000)
        numberOfRows()==1
    }

    def validateBundleIsNotListed(bName){
        filterByName(bName)
        sleep(2000)
        noRecordsMsg.displayed
    }

    def verifyRowsDisplayed(){

        numberOfRows()>0
    }
    /**
     * Selects planning or non-planing option according to parameter received
     * @param plan
     */
    def selectPlanningOption(plan){
        isPlanningSelector.click()
        if(plan == true){
            planSelect<< Keys.chord(Keys.DOWN)

        }else{
            planSelect<< Keys.chord(Keys.DOWN)
            planSelect<< Keys.chord(Keys.DOWN)
        }
        planSelect<< Keys.chord(Keys.ENTER)
    }


    def clearPlanningFilter(){
        clearPlanningFilter.click()
    }

    def clearNameFilter(){
       waitFor{ clearName.click()}
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
        waitFor{createButton.click()}
    }

    def numberOfRows(){
        individualRows.size()
    }

    def filterByName(name){
        nameFilter=name
        sleep(1000)
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



    /**
     * selecta the filter type for description
     * @return
     */

    def selectDescFilter(){
        descFilterKind.click()
        waitFor{$("li.k-item", text:"Contains").click()}
    }

    def clickOnBundle(){
        $('kendo-grid-list').find('a')[0].click()
        sleep(1500)
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
        individualRows.find("[role='gridcell']")[1].text()==desc
    }

    def selectByName(name){
        filterByName(name)
        $("a.cell-url-element", text:name).click()
        sleep(1000)
    }

    /**
     * Returns true if description and tick presence are as expected.
     * name has already been validated
     * @param data
     * @return
     */

    def validateBundleRowData(data){
        validateFilteredDescription(data[1])
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
            if(!individualRows[i].find(("[role='gridcell']")[3]).contains(qty)) {
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
            if(!individualRows[i].find("[role='gridcell']")[5].text().contains(dte)) {
                validation=false
                break
            }
        }
        return validation
    }

    def validateCompletionDate(dte){
        def validation=true
        for (int i=1;i<numberOfRows();i++){
            if(!rows[i].find("[role='gridcell']")[6].text().contains(dte)) {
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

