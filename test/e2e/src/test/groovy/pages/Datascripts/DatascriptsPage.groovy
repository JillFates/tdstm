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
    }


}