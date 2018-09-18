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

        rowsDisplayed       {$("#personIdGrid").find("role":"row")}
        //GRID FILTERS
        firstNameFilter             {$("input#gs_firstname")}
        middleNameFilter            {$("#gs_middlename   ")}
        lastNameFilter              {$("#gs_lastname")}
        userNameFilter              {$("#gs_userLogin")}
        emailFilter                 {$("#gs_email")}
        companyFilter               {$("#gs_company")}
        dateCreatedFilter           {$("#gs_dateCreated")}
        lastUpdatedFilter           {$("#gs_lastUpdated")}

        staffGridPager              { $("div#pg_personIdGridPager")}

        createStaffModal(required: false, wait:true) {$ ("div", class:"modal fade in")}
        manageStaffModal(required: false, wait:true) { $("div", "window-class":"modal-task")}

        adminModule { module AdminModule}
        bulkDeleteConfirmationModal { $('div#bulkDeleteModal')}
        deleteAssociatedAppOwnerOrSMEsInput { $('input#deleteIfAssocWithAssets')}
        deleteConfirmationModalButton { $('button#bulkModalDeleteBtn')}
        closeConfirmationModalButton { $('button#bulkModalCloseBtn')}
        rowInputsCheckbox {staffGridRows.find("td input")}
        commonsModule { module CommonsModule }
    }


    def filterByFirstName(fName){
        waitFor{firstNameFilter.displayed}
        firstNameFilter=fName
        commonsModule.waitForLoadingMessage()
    }

    def filterByMiddleName(mName){
        waitFor{middleNameFilter.displayed}
        middleNameFilter=mName
        commonsModule.waitForLoadingMessage()
    }

    def filterByLastname(lName){
        waitFor{lastNameFilter.displayed}
        lastNameFilter = lName
        commonsModule.waitForLoadingMessage()
    }

    def filterByUserName(usrName){
        waitFor{userNameFilter.displayed}
        userNameFilter=usrName
        commonsModule.waitForLoadingMessage()
    }

    def filterUserByEmail(email){
        waitFor{emailFilter.displayed}
        emailFilter=email
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

    def rowsDisplayed(){
        rowsDisplayed.displayed
    }

    def clearAllFilters(){
        clearfistName()
        clearMidName()
        clearLastName()
        clearUsrNme()
        clearEmail()
        clearCompany()
        clearDateCreated()
        clearLastUpdtd()
    }

    def clearfistName(){
        firstNameFilter=""
    }
    def clearMidName(){
        middleNameFilter=""
    }
    def clearLastName(){
        lastNameFilter=""
    }
    def clearUsrNme(){
        userNameFilter=""
    }

    def clearEmail(){
        emailFilter=""
    }
    def clearCompany(){
        companyFilter=""
    }
    def clearDateCreated(){
        dateCreatedFilter=""
    }
    def clearLastUpdtd(){
        lastUpdatedFilter=""
    }

    def isExpectedUser(firstName,midName,lstName,email,company){
        waitFor{$("td", "role": "gridcell", "aria-describedby": "personIdGrid_firstname").find("a").text() == firstName}
        waitFor{$("td", "role": "gridcell", "aria-describedby": "personIdGrid_middlename").find("a").text()==(midName)}
        waitFor{$("td", "role": "gridcell", "aria-describedby": "personIdGrid_lastname").find("a").text()==(lstName)}
        waitFor{$("td", "role": "gridcell", "aria-describedby": "personIdGrid_email").text()==(email)}
        waitFor{$("td", "role": "gridcell", "aria-describedby": "personIdGrid_company").text()==(company)}
    }

    def verifyNoRecordsDisplayed(){
        commonsModule.verifyElementDisplayed $(".ui-paging-info")
    }
}

