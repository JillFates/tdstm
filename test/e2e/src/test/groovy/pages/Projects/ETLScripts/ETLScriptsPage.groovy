package pages.Projects.ETLScripts

import geb.Page
import modules.CommonsModule
import geb.Browser
import modules.ProjectsModule

class ETLScriptsPage extends Page {
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
        firstDSEditButton { dsTableRows[0].find("td", "aria-colindex": "1").find("button span", class: "glyphicon-pencil")}
        projectsModule { module ProjectsModule}
    }

    def filterByName(name){
        scrollLeft()
        nameFilter = name
    }

    def clickOnEditButtonForFirstDS(){
        waitFor{firstDSEditButton.click()}
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
        waitFor{nameGridHeader.find("a span").click()}
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