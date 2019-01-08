package modules

import geb.Module

class AdminModule extends Module{

    static at = {
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
        menuContainer { $("div", class: "container menu-top-container menu-top-container-full-menu") }
        menuBar { menuContainer.find("div#navbar-collapse") }

        adminItem { menuBar.find("li.menu-parent-admin") }
        adminMenu { adminItem.find("ul", class: "dropdown-menu menu-item-expand") }
        adminSections { adminMenu.find("li", class: "menu-parent-item") }
        adminPortalItem { adminMenu.find("li.menu-admin-portal") }
        adminLMItem { adminMenu.find("li.menu-admin-license-manager").find("a", "href": "/tdstm/module/license/admin/list") }
        adminNoticesItem { adminMenu.find("li.menu-admin-license-manager").find("a", "href": "/tdstm/app/notice/list") }
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
        adminItem.click()
    }

    def goToAdminPortal(){
        selectMenu(adminItem)
        selectItem(adminPortalItem)
    }

    def goToAdminListStaff(){
        selectMenu(adminItem)
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
        selectMenu(adminItem)
        selectItem(adminCompaniesItem)
    }

    def goToListUsers(){
        selectMenu(adminItem)
        selectItem(adminUsersItem)
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
