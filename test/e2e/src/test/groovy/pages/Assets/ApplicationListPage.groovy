package pages.Assets

import geb.Page

class ApplicationListPage extends Page {

    static at = {
        alPageTitle.text().trim()  == "Application List"
        alPageBreadcrumbs[0].text()   == "Assets"
        alPageBreadcrumbs[1].text()   == "Applications"

// TODO following item have the elements inside the label and cannot be reached
        // alHeaderBarTitle.text().trim() == "Application:"
        alCreateAppBtn.value()        == "Create App"
        alBulkDeleteBtn.value()       == "Bulk Delete"
        alClearFiltersBtn.value()     == "Clear Filters"
// TODO following item have the elements inside the label and cannot be reached
        // alJustPlanningCBox.text().trim() == "Just Planning"
    }

    static content = {
        alPageTitle(wait:true)      { $("section", 	class:"content-header").find("h1") }
        alPageBreadcrumbs           { $("ol", class:"breadcrumb").find("li a")}
        alPageMessage (required: false, wait:true) { $("div#messageId")}
        alCompanySelector           { $('select#filterSelect', name:'companyId')}
        alCompanySelectorDefault    { alCompanySelector.find("option", selected:"selected") }

        alView                      { $("div#gview_applicationIdGrid")}
        alViewHeaderBar             { alView.find("div", class:"ui-jqgrid-titlebar ui-widget-header ui-corner-top ui-helper-clearfix")}
// TODO following item have the elements inside the label
        //  alHeaderBarTitle          { $("span", class:"ui-jqgrid-title") }
        alCreateAppBtn(wait:true)   { alViewHeaderBar.find("input", type: "button", "onclick":startsWith("EntityCrud.showAssetCreateView"))}
        alBulkDeleteBtn             { alViewHeaderBar.find("input#deleteAssetId", type: "button") }
        alJustPlanningCBox          { alViewHeaderBar.find("input#justPlanning", type: "checkbox") }
        alClearFiltersBtn           { alViewHeaderBar.find("input",class:"clearFilterId", type: "button") }
        alToggleListBtn             { alViewHeaderBar.find("a", class:"ui-jqgrid-titlebar-close HeaderButton")}

        alGridHeader                { alView.find("div",class:"ui-jqgrid-hbox")}
        alColumnsHeader             { alGridHeader.find("tr", class: "ui-jqgrid-labels ui-sortable", "role":"rowheader")}
        alSearchToolbar             { alGridHeader.find("tr", class: "ui-search-toolbar", "role":"rowheader")}

        alGridHeaderCols            { alColumnsHeader.find("div", class:"ui-jqgrid-sortable")} // TODO Use this reference to find the user preferece columns
        alSelectAllappCBox          { alColumnsHeader.find("input#cb_applicationIdGrid") }

        alNameColHeader             { alColumnsHeader.find("div#jqgh_assetName")}
        alNameFilter                { $("input#gs_assetName") }

        alGridRows                  { alView.find("table#applicationIdGrid").find("tr","role":"row", class:"ui-widget-content jqgrow ui-row-ltr")}
        alGridSize                  { alGridRows.size()}
        alRowSize                   { alGridHeaderCols.size()}

        alFirstAppName              { alGridRows[0].find("td","aria-describedby":"applicationIdGrid_assetName").find("a")}
        alFirstAppActions           { alGridRows[0].find("td", "aria-describedby":"applicationIdGrid_act")}
        alFirstAppEdit              { alFirstAppActions.find("a", href:contains("EntityCrud.showAssetEditView"))}
        alFirstCreateShowTasks      { alFirstAppActions.find("a", id:startsWith("icon_task"))}
        alFirstCreateShowComments   { alFirstAppActions.find("a", href:contains("icon_comment"))}
        alFirstAppClone             { alFirstAppActions.find("a", href:contains("EntityCrud.cloneAssetView"))}

        alGridPager                 { $("div#pg_applicationIdGridPager")}
        alCreateappModal            (required: false, wait:true) {$ ("div", class:"modal fade in")}
        alManageappModal            (required: false, wait:true) { $("div", "window-class":"modal-task")}


    }
}
