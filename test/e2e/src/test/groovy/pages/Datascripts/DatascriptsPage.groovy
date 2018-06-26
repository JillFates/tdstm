package pages.Datascripts

import geb.Page

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

        //First Element of the Datascripts Table
        firstDS(wait:true) { $("tr" ,  class:"k-state-selected").find("td")[1]}
        dsTableRows { $('tr[data-kendo-grid-item-index]')}
        firstDSName { dsTableRows[0].find("td", "aria-colindex": "2")}
        firstDSProvider { dsTableRows[0].find("td", "aria-colindex": "3")}
        firstDSDescription { dsTableRows[0].find("td", "aria-colindex": "4")}
        firstDSMode { dsTableRows[0].find("td", "aria-colindex": "5")}
        firstDSEditButton { dsTableRows[0].find("td", "aria-colindex": "1").find("button span", class: "glyphicon-pencil")}
    }

    def filterByName(name){
        nameFilter = name
    }

    def clickOnEditButtonForFirstDS(){
        waitFor{firstDSEditButton.click()}
    }

    def clickOnFirstGridRow(){
        waitFor{firstDSName.click()}
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
}