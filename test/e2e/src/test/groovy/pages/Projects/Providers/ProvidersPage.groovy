package pages.Projects.Providers

import geb.Page
import modules.CommonsModule
import modules.ProjectsMenuModule

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
        firstProviderTable(wait:true) { $("div", class:"k-virtual-content").find("div" , class:"k-grid-table-wrap")}
        firstProviderDate(wait:true) { firstProviderTable.find("tbody").find("tr").find("td")[2]}
        firstProviderName(wait:true) { firstProviderTable.find("tbody").find("tr").find("td")[0]}
        firstProviderDesc(wait:true) { firstProviderTable.find("tbody").find("tr").find("td")[1]}
        firstProviderDeleteButton(wait:true) {$("div", class:"tds-action-button-set")[0].find("tds-button-delete")}
        firstProviderEditPencilBtn(wait:true) {$("div", class: "tds-action-button-set").find("tds-button-edit")}
        noRecordsRow {$('.k-grid-norecords')}
        noRecordsMessage {noRecordsRow.find("td")}
        projectsModule { module ProjectsMenuModule}
        arrayOfProviders {$(class:"k-virtual-content").find(class:"k-grid-table-wrap", role:"presentation").find("tbody", role:"presentation").find("tr")}
        deleteConfirmationModal(required: false) {$(id:"providerAssociated").find(class:"modal-md").find(class:"modal-content")}
        deleteConfirmationNoBtn {deleteConfirmationModal.find(class:"form-group").find("button", class:"pull-right")}
        deleteConfirmationYesBtn {deleteConfirmationModal.find(class:"form-group").find("button", class:"pull-left")}
    }

    def filterByName(provName){
        nameFilter = provName
    }

    def clickOnFirstProviderName(){
        waitFor(30) {firstProviderName.click()}
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
     * are displayed.
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
                waitFor(30){deleteConfirmationModal.isDisplayed()}
                waitFor(30){deleteConfirmationYesBtn.click()}
                providersToDelete--
            }
        }
        else{
            println("There are no providers to delete")
            true
        }
    }

}
