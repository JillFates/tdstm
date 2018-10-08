package pages.Projects.ETLScripts

import geb.Page
import modules.CommonsModule

class CreateETLScriptsPage extends Page{

    static at = {
        title == "ETL Scripts"
        modaltitle.text() == "Create ETL Script"
        providerDdownName.text() == "Provider: *"
        datascriptDesc.text() == "Description:"
        datascriptName.text() == "Name: *"

    }

    static content = {
        modaltitle(required:false) { $("div", class:"modal-header").find("h4" , class:"modal-title")[0]}
        providerDdownName { $("label", for:"dataScriptProvider")}
        datascriptName { $("label", for:"dataScriptName")}
        datascriptDesc { $("label", for:"dataScriptDescription")}
        datascriptSaveBtn  { $("button", class:"btn btn-primary pull-left", type:"button")}
        datascriptXIcon {$("div", class:"modal-header").find("button","aria-label":"Close", class:"close")[0]}
        providerDropdown {$('span#dataScriptProvider')}

        //This gives you the latest created provider
        latestProvider {$("div", class:"k-list-scroller").find("li", class:"k-item")[1]}
        datascriptNameField   { $('input#dataScriptName')}
        datascriptDescField   { $('textarea#dataScriptDescription')}
        commonsModule { module CommonsModule }
    }

}
