package pages.Providers

import geb.Page

class ProvidersPage extends Page{
    static at = {
        title == "Providers"
        pageHeaderName.text() == "Providers"
        createBtn.text() == "Create Provider"
    }

    static content = {
        dateFilter { $("span" , class:"k-dateinput-wrap").find("input" , class:"k-input")}
        pageHeaderName { $("section", class:"content-header").find("h1")}
        createBtn(wait:true) { $('button#btnCreateProvider')}
        nameColumnHeader {$("div" , class:"sortable-column").find("label")[1]}
        nameFilter(wait:true) { $("input" ,  placeholder:"Filter Name")}
        descriptionFilter(wait:true) { $("input" ,  placeholder:"Filter Description")}
        refreshBtn {$("div" , class:"kendo-grid-toolbar__refresh-btn")}

        //First Element of the Providers Table
        firstProviderDate(wait:true) { $('td#k-grid0-r2c3')}
        firstProviderName(wait:true) { $('td#k-grid0-r2c1')}
        firstProviderDesc(wait:true) { $('td#k-grid0-r2c2')}
        firstProviderEditPencilBtn(wait:true) {$("div", class: "text-center").find("span", class: "glyphicon glyphicon-pencil")}
    }

}