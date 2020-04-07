package modules

import geb.Module

class AdminModule extends Module{

    static at = {
        waitFor {adminMenu.displayed}
        adminItem.text().trim() == "Admin"
        adminSections[0].text().trim() == "Administration"
        adminSections[1].text().trim() == "Manage Clients"
        adminSections[2].text().trim() == "Manage Workflows"
        adminSections[3].text().trim() == "Manage Model Library"
        adminPortalItem.text().trim() == "Admin Portal"
        adminLMItem.text().trim() == "License Admin"
        adminNoticesItem.text().trim() == "Notices"
        adminRolesItem.text().trim() == "Role Permissions"
        adminAssetOptItem.text().trim() == "Asset Options"
        adminCompaniesItem.text().trim() == "List Companies"
        adminStaffItem.text().trim() == "List Staff"
        adminUsersItem.text().trim() == "List Users"
        adminImportAccItem.text().trim() == "Import Accounts"
        adminExportAccItem.text().trim() == "Export Accounts"
        adminWorkflowsItem.text().trim() == "List Workflows"
        adminManufItem.text().trim() == "List Manufacturers"
        adminModelsItem.text().trim() == "List Models"
        adminExportManModItem.text().trim() == "Export Mfg & Models"
    }

    static content = {
        menuContainer { $("div", class: "menu-top-container") }
        menuBar { menuContainer.find("div#mobile-nav") }
        adminItem { menuBar.find("li.menu-parent-admin") }
        adminMenu { adminItem.find("ul", class: "dropdown-menu menu-item-expand") }
        adminSections { adminMenu.find("li", class: "menu-parent-admin") }
        adminPortalItem { adminMenu.find("li.menu-admin-portal") }
        adminLMItem { adminMenu.find("li.menu-admin-license-admin").find("a", "href": "/tdstm/module/license/admin/list") }
        adminNoticesItem { adminMenu.find("li.menu-admin-notice-manager").find("a", "href": "/tdstm/module/notice/list") }
        adminRolesItem { adminMenu.find("li.menu-admin-role") }
        adminAssetOptItem { adminMenu.find("li.menu-admin-asset-options") }
        adminCompaniesItem { adminMenu.find("li.menu-list-companies") }
        adminStaffItem { adminMenu.find("li.menu-list-staff") }
        adminUsersItem { adminMenu.find("li.menu-list-users") }
        adminImportAccItem { adminMenu.find("li.menu-client-import-accounts") }
        adminExportAccItem { adminMenu.find("li.menu-client-export-accounts") }
        adminWorkflowsItem { adminMenu.find("li.menu-list-workflows") }
        adminManufItem { adminMenu.find("li.menu-list-manufacturers") }
        adminModelsItem { adminMenu.find("li.menu-list-models") }
        adminExportManModItem { adminMenu.find("li.menu-sync-libraries") }
    }

    def selectMenu(menuItem) {
        if (menuItem.present) {
            if (menuItem.isDisplayed()) {
                if (!menuItem.hasClass("open")) {
                    waitFor { menuItem.click() }
                }
            }
        }
    }

    def selectItem(item){
        if (item.present) {
            if(item.isDisplayed()) {
                waitFor { item.click()}
            }
        }
    }

    def goToAdminMenu() {
        waitFor(30) {adminItem}
        waitFor{adminItem.displayed}
        waitFor(30){adminItem.click()}
    }

    def goToAdminPortal(){
        waitFor(30) {adminItem}
        selectMenu(adminItem)
        waitFor(30) {adminPortalItem}
        selectItem(adminPortalItem)
    }

    def goToAdminListStaff(){
        waitFor (30) {adminItem}
        selectMenu(adminItem)
        waitFor (30) {adminStaffItem}
        selectItem(adminStaffItem)
    }

    def goToLicenseAdmin() {
        selectMenu(adminItem)
        selectItem(adminLMItem)
    }

    def goToNoticesAdmin() {
        selectMenu(adminItem)
        selectItem(adminNoticesItem)
    }

    def goToRolePermissions() {
        selectMenu(adminItem)
        selectItem(adminRolesItem)
    }

    def goToAssetOptions(){
        selectMenu(adminItem)
        selectItem(adminAssetOptItem)
    }

    def goToListCompanies(){
        waitFor (30) {adminItem}
        selectMenu(adminItem)
        waitFor (30){selectItem(adminCompaniesItem)}
    }

    def goToListUsers(){
        waitFor (30) {selectMenu(adminItem)}
        waitFor (30){selectItem(adminUsersItem)}
    }

    def goToImportAccounts(){
        selectMenu(adminItem)
        selectItem(adminImportAccItem)
    }

    def goToExportAccounts(){
        selectMenu(adminItem)
        selectItem(adminExportAccItem)
    }

    def goToListWorkflows(){
        selectMenu(adminItem)
        selectItem(adminWorkflowsItem)
    }

    def goToListManufacturers(){
        selectMenu(adminItem)
        selectItem(adminManufItem)
    }

    def goToListModels(){
        selectMenu(adminItem)
        selectItem(adminModelsItem)
    }

    def goToExportModels(){
        selectMenu(adminItem)
        selectItem(adminExportManModItem)
    }
}
