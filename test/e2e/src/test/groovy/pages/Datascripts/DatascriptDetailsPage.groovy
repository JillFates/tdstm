package pages.Datascripts

import geb.Page

class DatascriptDetailsPage extends Page{

    static at = {
        title == "DataScripts"
        datascriptDetail.text() == "DataScript Detail"

    }

    static content = {
        modaltitle(required:false) { $("div", class:"modal-header").find("h4" , class:"modal-title")[0]}
        datascriptDetail { $('div.modal-header h4[_ngcontent-c7]')}
        dsDetailXIcon {$("div", class:"modal-header").find("button","aria-label":"Close", class:"close")[0]}

    }

}
