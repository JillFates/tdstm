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
        nameFilter(wait:true)  { $("input" ,  placeholder:"Filter Name")}

        //First Element of the Providers Table
        firstProvider(wait:true)  { $("tr" ,  class:"k-alt").find("td")[1]}
    }

}
