package pages.Projects.ETLScripts

import geb.Page
import utils.CommonActions
import modules.CommonsModule

class EditETLScriptsPage extends Page{

    static at = {
        title == "ETL Scripts"
        modaltitle.text() == "ETL Script Edit"
        providerDdownName.text() == "Provider: *"
        datascriptDesc.text() == "Description:"
        datascriptName.text() == "Name: *"
    }

    static content = {
        modaltitle { $("div", class:"modal-header").find("h4" , class:"modal-title")[0]}
        providerDdownName { $("label", for:"dataScriptProvider")}
        datascriptName { $("label", for:"dataScriptName")}
        datascriptDesc { $("label", for:"dataScriptDescription")}
        datascriptSaveBtn  { $("button", text: contains("Save"))}
        datascriptXIcon { $('div.modal.fade.in button.close')}
        providerDropdown { $('span#dataScriptProvider')}
        selectedProvider { providerDropdown.find("span.k-input")}
        //This gives you the latest created provider
        providers { $("div", class:"k-list-scroller").find("li", class:"k-item")}
        datascriptNameField { $('input#dataScriptName')}
        datascriptDescField { $('textarea#dataScriptDescription')}
        commonsModule { module CommonsModule }
    }

    def setDsName(name){
        datascriptNameField = name
    }

    def setDSDescription(description){
        datascriptDescField = description
    }

    def selectRandomProviderDisplayed(){
        waitFor{providerDropdown.click()}
        def provider
        if (providers.size() > 1){
            provider = CommonActions.getRandomOption providers
        } else {
            // only one provider so selecting that
            provider = providers
        }
        waitFor {provider.click()}
    }

    def getSelectedProviderText(){
        selectedProvider.text().trim()
    }

    def clickOnSaveButton(){
        waitFor{datascriptSaveBtn.click()}
        commonsModule.waitForDialogModalHidden()
    }
}