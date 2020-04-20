package pages.Projects.ETLScripts

import geb.Page
import modules.CommonsModule
import modules.ProjectsMenuModule

class ETLScriptsPage extends Page {
    static at = {
        title == "ETL Scripts"
        pageHeaderName.text() == "ETL Scripts"
        pageBreadcrumbs[0].text() == "Project"
        pageBreadcrumbs[1].text() == "ETL Scripts"
    }

    static content = {
        pageHeaderName { $(".content-header").find("h2")[0]}
        createBtn(wait:true) { $('clr-icon[shape="plus"]').closest("button")[0]}
        pageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        filterButton{ $('clr-icon[shape="filter"]').closest("button")[0]}
        nameFilter(required:false) { $("input")[0]}
        providerFilter (required:false) { $("input")[1]}
        descriptionFilter (required:false) { $("input")[2]}
        modeFilter (required:false) { $("input")[3]}
        dateCreateFilter (required:false) { $("input")[4]}
        lastUpdatedFilter(required:false) { $("input")[5]}
        removeFilterNameIcon { $('clr-icon[shape="times-circle"]').closest("button")[1]}
        removeFilterDescriptionIcon { $('clr-icon[shape="times-circle"]').closest("button")[1]}
        removeFilterProviderIcon { $('input[placeholder="Filter Provider"] + span.fa-times')}
        removeFilterModeIcon { $('input[placeholder="Filter Mode"] + span.fa-times')}
        nameGridHeader { $('th', "aria-colindex": "2")}
        commonsModule { module CommonsModule }
        //First Element of the Datascripts Table
        firstDS(wait:true) { dsTableRows[0]}
        dsTableRows (required:false){ $(".k-grid-table")[0].find("tr")}
        refreshGridIcon { $('button[title="Refresh"]')}
        //refresh button { $('input[shape="sync"]')}
        firstDSName { dsTableRows[0].find("td", "aria-colindex": "2")}
        firstDSProvider { dsTableRows[0].find("td", "aria-colindex": "3")}
        firstDSDescription { dsTableRows[0].find("td", "aria-colindex": "4")}
        firstDSMode { dsTableRows[0].find("td", "aria-colindex": "5")}
        projectsModule { module ProjectsMenuModule}
        actionsList { $('tds-button[icon="ellipsis-vertical"]')}
        viewAction (required:false) {$("a.dropdown-item")[0]}
        editAction (required:false) {$("a.dropdown-item")[1]}
        editScriptAction (required:false) {$("a.dropdown-item")[2]}
        deleteAction (required:false) {$("a.dropdown-item")[3]}
        confirmationModal (required:false) {$("div.modal-dialog.modal-confirm")}
        confirmYes (required:false) { $('clr-icon[shape="check"]').closest("button")[0]}
        noRecordsMessage (required:false){$("tr.k-grid-norecords", text:"No records available.")}


    }

    /**
     * Deletes a script based on its position
     * @author ingrid
     */
    def deleteByPosition(position){
        waitFor{actionsList[position].click()}
        deleteAction.click()
        waitFor{confirmYes.click()}
    }

    def clickOnFilterButton(){
        waitFor{filterButton.click()}
    }

    def filterByName(name){
        scrollLeft()
        nameFilter = name
        return name
    }

    def clickOnEditButtonForFirstDS(){
        waitFor{actionsList[0].click()}
        editAction.click()
    }

    def clickOnFirstGridRow(){
        scrollLeft()
        waitFor{firstDSName.click()}
    }

    def collectFirstDSInfoDisplayedInGrid(){
        def dsInfo = [:]
        dsInfo.putAll([
                "name": getFirstRowDSName(),
                "description": getFirstRowDSDescription(),
                "provider": getFirstRowDSProvider(),
                "mode": getFirstRowDSMode()
        ])
        dsInfo
    }

    def filterByDateCreated(date){
        commonsModule.setKendoDateFilter(date, 0)
    }

    def filterByDescription(description){
        scrollLeft()
        descriptionFilter = description
    }

    def filterByMode(name){
        scrollRight()
        modeFilter = name
    }

    def filterByProvider(name){
        scrollLeft()
        providerFilter = name
    }

    def getDSRowsSize(){
        dsTableRows.size()
    }

    def clickOnRefreshIcon(){
        waitFor{refreshGridIcon.click()}
    }

    def clickOnNameHeader(){
        scrollLeft()
        waitFor{nameGridHeader.click()}
    }

    def getFirstRowDSName(){
        scrollLeft()
        firstDSName.text().trim()
    }

    def getFirstRowDSDescription(){
        scrollRight()
        firstDSDescription.text().trim()
    }

    def getFirstRowDSProvider(){
        scrollLeft()
        firstDSProvider.text().trim()
    }

    def getFirstRowDSMode(){
        scrollRight()
        firstDSMode.text().trim()
    }

    def removeNameFilter(){
        scrollLeft()
        removeFilterNameIcon.click()
    }

    def removeDescriptionFilter(){
        scrollRight()
        removeFilterDescriptionIcon.click()
    }

    def removeDateCreateFilter(){
        commonsModule.removeKendoDateFilter(0)
    }

    def isOrderedByName(mode){
        if (mode == "asc"){
            waitFor{nameGridHeader.find("span.k-i-sort-asc-sm").displayed}
        } else {
            waitFor{nameGridHeader.find("span.k-i-sort-desc-sm").displayed}
        }
    }

    def scrollRight(){
        def gridWidth = browser.driver.executeScript('return $("kendo-grid").width()')
        browser.driver.executeScript("\$('.k-grid-header-wrap').scrollLeft($gridWidth)")
    }

    def scrollLeft(){
        browser.driver.executeScript('$(".k-grid-header-wrap").scrollLeft(0)')
    }
}