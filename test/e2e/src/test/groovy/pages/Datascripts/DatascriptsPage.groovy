package pages.Datascripts

import geb.Page
import modules.CommonsModule
import geb.Browser

class DatascriptsPage extends Page {
    static at = {
        title == "ETL Scripts"
        pageHeaderName.text() == "ETL Scripts"
        createBtn.text() == "Create ETL Script"
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        createBtn(wait:true) { $('button#btnCreateDataScript')}
        nameFilter(wait:true) { $("input" ,  placeholder:"Filter Name")}
        dateCreateFilter { $("input", class:"k-input")[0]}
        descriptionFilter { $("input", placeholder:"Filter Description")}
        providerFilter { $("input", placeholder:"Filter Provider")}
        modeFilter { $("input", placeholder:"Filter Mode")}
        removeFilterNameIcon { $('input[placeholder="Filter Name"] + span.fa-times')}
        removeFilterDescriptionIcon { $('input[placeholder="Filter Description"] + span.fa-times')}
        removeFilterProviderIcon { $('input[placeholder="Filter Provider"] + span.fa-times')}
        removeFilterModeIcon { $('input[placeholder="Filter Mode"] + span.fa-times')}
        nameGridHeader { $('th', "aria-colindex": "2")}
        commonsModule { module CommonsModule }
        //First Element of the Datascripts Table
        firstDS(wait:true) { $("tr" ,  class:"k-state-selected").find("td")[1]}
        dsTableRows { $('tr[data-kendo-grid-item-index]')}
        refreshGridIcon { $('span.glyphicon-refresh')}
        firstDSName { dsTableRows[0].find("td", "aria-colindex": "2")}
        firstDSProvider { dsTableRows[0].find("td", "aria-colindex": "3")}
        firstDSDescription { dsTableRows[0].find("td", "aria-colindex": "4")}
        firstDSMode { dsTableRows[0].find("td", "aria-colindex": "5")}
    }

    def filterByName(name){
        nameFilter = name
    }

    def filterByDateCreated(date){
        commonsModule.setKendoDateFilter(date, 0)
    }

    def filterByDescription(description){
        descriptionFilter = description
    }

    def filterByMode(name){
        modeFilter = name
    }

    def filterByProvider(name){
        providerFilter = name
    }

    def getDSRowsSize(){
        dsTableRows.size()
    }

    def clickOnRefreshIcon(){
        waitFor{refreshGridIcon.click()}
    }

    def clickOnFirstGridRow(){
        waitFor{firstDSName.click()}
    }

    def clickOnNameHeader(){
        nameGridHeader.click()
    }

    def collectFirstDSInfoDisplayedInGrid(){
        def dsInfo = [:]
        dsInfo.putAll([
                "name": firstDSName.text().trim(),
                "description": firstDSDescription.text().trim(),
                "provider": firstDSProvider.text().trim(),
                "mode": firstDSMode.text().trim()
        ])
        dsInfo
    }

    def removeNameFilter(){
        removeFilterNameIcon.click()
    }

    def removeDescriptionFilter(){
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
        browser.driver.executeScript('$(".k-grid-content").scrollLeft(500)')
    }

    def scrollLeft(){
        browser.driver.executeScript('$(".k-grid-content").scrollLeft(0)')
    }
}