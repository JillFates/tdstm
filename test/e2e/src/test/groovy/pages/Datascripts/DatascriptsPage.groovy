package pages.Datascripts

import geb.Page

class DatascriptsPage extends Page {
    static at = {
        title == "DataScripts"
        pageHeaderName.text() == "DataScripts"
        //createBtn.text() == "Create Provider"
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        //createBtn(wait:true) { $('button#btnCreateProvider')}
        //nameFilter(wait:true) { $("input" ,  placeholder:"Filter Name")}

        //First Element of the Datascripts Table
        //firstProvider(wait:true) { $("tr" ,  class:"k-alt").find("td")[1]}
    }


}