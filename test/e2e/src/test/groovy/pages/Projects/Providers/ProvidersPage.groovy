package pages.Projects.Providers

import geb.Page
import modules.CommonsModule
import modules.ProjectsModule

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
        commonsModule { module CommonsModule }
        //First Element of the Providers Table
        firstProviderDate(wait:true) { $('td#k-grid0-r2c3')}
        firstProviderName(wait:true) { $('td#k-grid0-r2c1')}
        firstProviderDesc(wait:true) { $('td#k-grid0-r2c2')}
        firstProviderDeleteButton(wait:true) { $('td#k-grid0-r2c0').find("button span.fa-trash")}
        firstProviderEditPencilBtn(wait:true) {$("div", class: "text-center").find("span", class: "glyphicon glyphicon-pencil")}
        noRecordsRow {$('.k-grid-norecords')}
        noRecordsMessage {noRecordsRow.find("td")}
        projectsModule { module ProjectsModule}
        arrayOfProviders {$(class:"k-virtual-content").find(class:"k-grid-table-wrap", role:"presentation").find("tbody", role:"presentation").find("tr")}

    }

    def filterByName(provName){
        nameFilter = provName
    }

    def clickOnFirstProviderName(){
        waitFor {firstProviderName.click()}
    }

    def noRecordsRowSize(){
        waitFor{noRecordsRow[1].displayed}
        noRecordsRow.size()
    }

    def getNoRecordsMessageText(){
        noRecordsMessage[1].text().trim()
    }

    def getFirstRowProviderGridName(){
        firstProviderName.text().trim()
    }

    def clickOnFirstProviderDeleteActionButton(){
        waitFor{firstProviderDeleteButton.click()}
    }

    /**
     * The objective of this method is to cleanup the records on the Providers List.
     * We need at least 3 providers to play with, which is why you'll see that no providers are deleted if 3 or less
     * are the only ones displayed.
     * Also, we will delete a max of 8 records per run.
     * @author Alvaro Navarro
     */
    def cleanUpProviders() {
        def amountOfProviders = arrayOfProviders.size()
        def providersLeft = amountOfProviders - 3
        if(providersLeft > 0)
        {
           def providersToDelete
            if((providersLeft-8) > 0)
                providersToDelete=8
            else
                providersToDelete=providersLeft

            while(providersToDelete!=0)
            {
                clickOnFirstProviderDeleteActionButton()
                commonsModule.waitForPromptModalDisplayed()
                commonsModule.clickOnDeleteYesPromptModal()
                commonsModule.waitForPromptModalHidden()
                providersToDelete--
            }
        }
    }

}