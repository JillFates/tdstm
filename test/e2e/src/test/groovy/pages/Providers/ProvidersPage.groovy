package pages.Providers

import geb.Page

class ProvidersPage extends Page{
    static at = {
        title == "Providers"
        pageHeaderName.text() == "Providers"
        createBtn.text() == "Create Provider"
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        createBtn(wait:true)   { $('button#btnCreateProvider')}
        nameColumnHeader {$("div" , class:"sortable-column").find("label")[1]}
        nameFilter(wait:true)  { $("input" ,  placeholder:"Filter Name")}
        descriptionFilter(wait:true) { $("input" ,  placeholder:"Filter Description")}
        refreshBtn {$("div" , class:"kendo-grid-toolbar__refresh-btn")}

        //First Element of the Providers Table
        firstProviderName(wait:true)  { $("tr" ,  class:"k-alt").find("td")[1]}
        firstProviderDesc(wait:true)  { $("tr" ,  class:"k-alt").find("td")[2]}

    }

}
