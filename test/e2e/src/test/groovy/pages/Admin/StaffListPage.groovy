package pages.Admin

import geb.Page
import modules.AdminModule
import modules.CommonsModule

class   StaffListPage extends Page {

    static at = {
        title == "Staff List"
        staffPageTitle.text().trim() == "Staff List"
        pageBreadcrumbs[0].text() == "Admin"
        pageBreadcrumbs[1].text() == "Client"
        pageBreadcrumbs[2].text() == "Staff"

    // TODO following item have the elements inside the label and cannot be reached
        createStaffBtn.value() == "Create Staff"
        compareMergeBtn.value() == "Compare/Merge"
        bulkDeleteBtn.value()== "Bulk Delete"
    }

    static content = {
        staffPageTitle(wait:true)   { $("section", 	class:"content-header").find("h1")}
        pageBreadcrumbs             { $("ol", class:"breadcrumb").find("li a")}
        pageMessage (required: false, wait:true) { $("div", class:"message").not("div", class:"nodisplay")}
        companySelector             { $('select#filterSelect', name:'companyId')}
        companySelectorDefault      { companySelector.find("option", selected:"selected")}

        staffView                   { $("div#gview_personIdGrid")}
        staffViewHeaderBar          { staffView.find("div", class:"ui-jqgrid-titlebar ui-widget-header ui-corner-top ui-helper-clearfix")}
        // TODO following item have the elements inside the label
        createStaffBtn(wait:true)   { staffViewHeaderBar.find("input", type: "button", class: "create")}
        compareMergeBtn             { staffViewHeaderBar.find("input#compareMergeId", type: "button") }
        bulkDeleteBtn               { staffViewHeaderBar.find("input#bulkDelete", type: "button") }
        toggleListBtn               { staffViewHeaderBar.find("a", class:"ui-jqgrid-titlebar-close HeaderButton")}

        staffGridHeader             { staffView.find("div#ui-jqgrid-hbox")}
        columnsHeader               { staffGridHeader.find("tr", class: "ui-jqgrid-labels ui-sortable")}
        searchToolbar               { staffGridHeader.find("tr", class: "ui-search-toolbar")}
        staffGridHeaderCols         { columnsHeader.find("div", class:"ui-jqgrid-sortable")}
        staffGridRows               { staffView.find("table#personIdGrid").find("tr.ui-widget-content","role":"row")}
        gridSize                    { staffGridRows.size()}
        rowSize                     { staffGridHeaderCols.size()}
        //Grid Columns
        emailColumn                 {$("#jqgh_email")}

        selectAllStaffCBox          { columnsHeader.find("input#cb_personIdGrid")}

        firstNameFilter             { $("input#gs_firstname")}

        staffGridPager              { $("div#pg_personIdGridPager")}

        createStaffModal(required: false, wait:true) {$ ("div", class:"modal fade in")}
        manageStaffModal(required: false, wait:true) { $("div", "window-class":"modal-task")}

        adminModule { module AdminModule}
        bulkDeleteConfirmationModal { $('div#bulkDeleteModal')}
        deleteAssociatedAppOwnerOrSMEsInput { $('input#deleteIfAssocWithAssets')}
        deleteConfirmationModalButton { $('button#bulkModalDeleteBtn')}
        closeConfirmationModalButton { $('button#bulkModalCloseBtn')}
        lastNameFilter { $("input#gs_lastname")}
        rowInputsCheckbox {staffGridRows.find("td input")}
        commonsModule { module CommonsModule }
    }

    def filterByLastname(name){
        waitFor{lastNameFilter.displayed}
        lastNameFilter = name
        commonsModule.waitForLoadingMessage()
    }

    def getGridRowsSize(){
        gridSize
    }

    def selectRow(){
        rowInputsCheckbox.click()
    }

    def clickOnBulkDeleteButton(){
        waitFor{bulkDeleteBtn.click()}
        waitFor{bulkDeleteConfirmationModal.jquery.attr("class").contains("in")}
    }

    def clickOnDeleteAssociatedAppOwnerOrSMEsInput(){
        waitFor{deleteAssociatedAppOwnerOrSMEsInput.click()}
    }

    def clickOnDeleteConfirmationModalButton(){
        waitFor{deleteConfirmationModalButton.click()}
    }

    def clickOnCloseConfirmationModalButton(){
        waitFor{closeConfirmationModalButton.click()}
    }
}
