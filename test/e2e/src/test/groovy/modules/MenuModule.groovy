package modules

import geb.Module

class MenuModule extends Module {

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

        projectsItem.text().trim() == "Projects"
        projectsActiveItem.text().trim() == "Active Projects"
        projectsCurrentItem.text().contains() == "Details" //      TODO add the project name as test property
        projectsMailItem.text().trim() == "User Activation Emails"
        projectsStaffItem.text().trim() == "Project Staff"
        projectsFieldsSetItem.text().trim() == "Asset Field Settings"

        tasksItem.text().trim() == ""
        tasksSections.text().trim() == ""
        tasksMyTasksItem.text().trim() == ""
        tasksManagerItem.text().trim() == ""
        tasksGraphItem.text().trim() == ""
        tasksTimelineItem.text().trim() == ""
        tasksCookbookItem.text().trim() == ""
        tasksGenHistoryItem.text().trim() == ""
        tasksImportItem.text().trim() == ""

    }

    static content = {

        menuContainer       { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar             { menuContainer.find("div#navbar-collapse") }

        adminItem           (required:false) { menuBar.find("li.menu-parent-admin") }
        adminMenu           (required:false) { adminItem.find("ul",class:"dropdown-menu menu-item-expand")}
        adminSections       (required:false) { adminMenu.find("li",class:"menu-parent-item")}
        adminPortalItem     (required:false) { adminMenu.find("li.menu-admin-portal")}
        adminLMItem         (required:false) { adminMenu.find("li.menu-admin-license-manager").find("a","href":"/tdstm/app/license/admin/list")}
        adminNoticesItem    (required:false) { adminMenu.find("li.menu-admin-license-manager").find("a","href":"/tdstm/app/notice/list")}
        adminRolesItem      (required:false) { adminMenu.find("li.menu-admin-role")}
        adminAssetOptItem   (required:false) { adminMenu.find("li.menu-admin-asset-options")}
        adminCompaniesItem  (required:false) { adminMenu.find("li.menu-list-companies")}
        adminStaffItem      (required:false) { adminMenu.find("li.menu-list-staff")}
        adminUsersItem      (required:false) { adminMenu.find("li.menu-list-users")}
        adminImportAccItem  (required:false) { adminMenu.find("li.menu-client-import-accounts")}
        adminExportAccItem  (required:false) { adminMenu.find("li.menu-client-export-accounts")}
        adminWorkflowsItem  (required:false) { adminMenu.find("li.menu-list-workflows")}
        adminManufItem      (required:false) { adminMenu.find("li.menu-list-manufacturers")}
        adminModelsItem     (required:false) { adminMenu.find("li.menu-list-models")}
        adminExportManModItem (required:false) { adminMenu.find("li.menu-sync-libraries")}

        projectsItem        (required:false) { menuBar.find("li.menu-parent-projects") }
        projectsMenu        (required:false) { projectsItem.find("ul",class:"dropdown-menu menu-item-expand")}
        projectsActiveItem  (required:false) { projectsMenu.find("li.menu-projects-active-projects")}
        projectsCurrentItem (required:false) { projectsMenu.find("li.menu-projects-current-project")}
        projectsStaffItem   (required:false) { projectsMenu.find("li.menu-projects-user-activation")}
        projectsMailItem    (required:false) { projectsMenu.find("li.menu-projects-user-activation")}
        projectsFieldsSetItem  (required:false) { projectsMenu.find("li.menu-projects-field-settings")}

        tasksItem           (required:false) { menuBar.find("li.menu-parent-tasks") }
        tasksMenu           (required:false) { tasksItem.find("ul",class:"dropdown-menu menu-item-expand")}
        tasksSections       (required:false) { tasksMenu.find("li",class:"menu-parent-item")}
        tasksMyTasksItem    (required:false) { tasksMenu.find("li.menu-parent-tasks-my-tasks")}
        tasksManagerItem    (required:false) { tasksMenu.find("li.menu-parent-tasks-task-manager")}
        tasksGraphItem      (required:false) { tasksMenu.find("li.menu-parent-tasks-task-graph")}
        tasksTimelineItem   (required:false) { tasksMenu.find("li.menu-parent-tasks-task-timeline")}
        tasksCookbookItem   (required:false) { tasksMenu.find("li.menu-parent-tasks-cookbook")}
        tasksGenHistoryItem (required:false) { tasksMenu.find("li.menu-parent-tasks-generation-history")}
        tasksImportItem     (required:false) { tasksMenu.find("li.menu-parent-tasks-import-tasks")}

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
                waitFor { item.click() }
            }
        }
    }

    def goToAdminPortal(){
        selectMenu(adminItem)
        selectItem(adminPortalItem)
    }

    def goToAdminListStaff(){
        selectMenu(adminItem)
        selectItem(adminStaffItem)
    }

    def goToProjectsActive(){
        selectMenu(projectsItem)
        selectItem(projectsActiveItem)
    }

    def goToTasksManager(){
        selectMenu(tasksItem)
        selectItem(tasksManagerItem)
    }

    def goToTasksCookbook(){
        selectMenu(tasksItem)
        selectItem(tasksCookbookItem)
    }

// TODO Add remains menu items here


}
