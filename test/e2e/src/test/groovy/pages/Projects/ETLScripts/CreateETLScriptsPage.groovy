package pages.Projects.ETLScripts

import geb.Page
import modules.CommonsModule

class CreateETLScriptsPage extends Page{

    static at = {
        title == "ETL Scripts"
        modaltitle.text() == "ETL Script Create"
        providerDdownName.text() == "Provider: *"
        datascriptDesc.text() == "Description:"
        datascriptName.text() == "Name: *"

    }

    static content = {
        modaltitle(required:false) { $("div", class:"modal-header").find("h3" , class:"modal-title")[0]}
        providerDdownName { $("label", for:"dataScriptProvider")}
        datascriptName { $("label", for:"dataScriptName")}
        datascriptDesc { $("label", for:"dataScriptDescription")}
        datascriptSaveBtn  { $('clr-icon[shape="floppy"]').closest("button")[0]}
        datascriptXIcon { $('clr-icon[shape="close"]').closest("button")[0]}
        providerDropdown {$('#dataScriptProvider span.k-select')}

        //This gives you the latest created provider
        latestProvider {$("div", class:"k-list-scroller").find("li", class:"k-item")[1]}
        datascriptNameField   { $('input#dataScriptName')}
        datascriptDescField   { $('textarea#dataScriptDescription')}
        commonsModule { module CommonsModule }

    }


}
