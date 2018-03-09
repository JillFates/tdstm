package modules

import geb.Module

class MenuModule extends Module {

    static at = {
        
        adminItem.text().trim()         == "Admin"
        adminSections[0].text().trim()  == "Administration"
        adminSections[1].text().trim()  == "Manage Clients"
        adminSections[2].text().trim()  == "Manage Workflows"
        adminSections[3].text().trim()  == "Manage Model Library"
        adminPortalItem.text().trim()   == "Admin Portal"
        adminLMItem.text().trim()       == "License Admin"
        adminNoticesItem.text().trim()  == "Notices"
        adminRolesItem.text().trim()    == "Role Permissions"
        adminAssetOptItem.text().trim() == "Asset Options"
        adminCompaniesItem.text().trim()== "List Companies"
        adminStaffItem.text().trim()    == "List Staff"
        adminUsersItem.text().trim()    == "List Users"
        adminImportAccItem.text().trim()== "Import Accounts"
        adminExportAccItem.text().trim()== "Export Accounts"
        adminWorkflowsItem.text().trim()== "List Workflows"
        adminManufItem.text().trim()    == "List Manufacturers"
        adminModelsItem.text().trim()   == "List Models"
        adminExportManModItem.text().trim() == "Export Mfg & Models"

        projectsItem.text().trim()          == "Projects"
        projectsActiveItem.text().trim()    == "Active Projects"
        projectsCurrentItem.text().contains() == "Details" //      TODO add the project name as test property
        projectsMailItem.text().trim()      == "User Activation Emails"
        projectsStaffItem.text().trim()     == "Project Staff"
        projectsFieldsSetItem.text().trim() == "Asset Field Settings"

        assetsItem.text().trim()            == "Assets"
        assetsSections[0].text().trim()     == "Assets"
        assetsSummaryItem.text().trim()     == "Summary Table"
        assetsAllAssetsItem.text().trim()   == "All Assets"
        assetsApplicationsItem.text().trim()== "Applications"
        assetsDevicesItem.text().trim()     == "Devices"
        assetsServersItem.text().trim()     == "Servers"
        assetsDatabasesItem.text().trim()   == "Databases"
        assetsStorageItem.text().trim()     == "Storage-Devices"
        assetsLogicalItem.text().trim()     == "Storage-Logical"
        assetsDependItem.text().trim()      == "Dependencies"
        assetsCommentsItem.text().trim()    == "Comments"
        assetsDepAnalyzerItem.text().trim() == "Dependency Analyzer"
        assetsArchGraphItem.text().trim()   == "Architecture Graph"
        assetsImportItem.text().trim()      == "Import Assets"
        assetsManageBatchesItem.text().trim() == "Manage Batches"
        assetsExportItem.text().trim()      == "Import Assets"
        assetsViewManagerItem.text().trim() == "View Manager"

        tasksItem.text().trim()             == "Tasks"
        tasksSections[0].text().trim()      == "Tasks"
        tasksMyTasksItem.text().trim()      == "My Tasks ( " +taskCount+ " )"
        tasksManagerItem.text().trim()      == "Task Manager"
        tasksGraphItem.text().trim()        == "Task Graph"
        tasksTimelineItem.text().trim()     == "Task Timeline"
        tasksCookbookItem.text().trim()     == "Cookbook"
        tasksGenHistoryItem.text().trim()   == "Generation History"
        tasksImportItem.text().trim()       == "Import Tasks"

    }

    static content = {

        menuContainer       { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar             { menuContainer.find("div#navbar-collapse") }

        adminItem               { menuBar.find("li.menu-parent-admin") }
        adminMenu               { adminItem.find("ul",class:"dropdown-menu menu-item-expand")}
        adminSections           { adminMenu.find("li",class:"menu-parent-item")}
        adminPortalItem         { adminMenu.find("li.menu-admin-portal")}
        adminLMItem             { adminMenu.find("li.menu-admin-license-manager").find("a","href":"/tdstm/app/license/admin/list")}
        adminNoticesItem        { adminMenu.find("li.menu-admin-license-manager").find("a","href":"/tdstm/app/notice/list")}
        adminRolesItem          { adminMenu.find("li.menu-admin-role")}
        adminAssetOptItem       { adminMenu.find("li.menu-admin-asset-options")}
        adminCompaniesItem      { adminMenu.find("li.menu-list-companies")}
        adminStaffItem          { adminMenu.find("li.menu-list-staff")}
        adminUsersItem          { adminMenu.find("li.menu-list-users")}
        adminImportAccItem      { adminMenu.find("li.menu-client-import-accounts")}
        adminExportAccItem      { adminMenu.find("li.menu-client-export-accounts")}
        adminWorkflowsItem      { adminMenu.find("li.menu-list-workflows")}
        adminManufItem          { adminMenu.find("li.menu-list-manufacturers")}
        adminModelsItem         { adminMenu.find("li.menu-list-models")}
        adminExportManModItem   { adminMenu.find("li.menu-sync-libraries")}

        projectsItem            { menuBar.find("li.menu-parent-projects") }
        projectsMenu            { projectsItem.find("ul",class:"dropdown-menu menu-item-expand")}
        projectsActiveItem      { projectsMenu.find("li.menu-projects-active-projects")}
        projectsCurrentItem     { projectsMenu.find("li.menu-projects-current-project")}
        projectsStaffItem       { projectsMenu.find("li.menu-projects-user-activation")}
        projectsMailItem        { projectsMenu.find("li.menu-projects-user-activation")}
        projectsFieldsSetItem   { projectsMenu.find("li.menu-projects-field-settings")}

        assetsItem              { menuBar.find("li.menu-parent-assets") }
        assetsMenu              { assetsItem.find("ul",class:"dropdown-menu menu-item-expand")}
        assetsSections          { assetsMenu.find("li",class:"menu-parent-item")}
        assetsSummaryItem       { assetsMenu.find("li.menu-parent-assets-summary-table")}
        assetsAllAssetsItem     { assetsMenu.find("li.menu-parent-assets-asset-explorer")}
        assetsApplicationsItem  { assetsMenu.find("li.menu-parent-assets-application-list")}
        assetsDevicesItem       { assetsMenu.find("li.menu-parent-assets-all-list")}
        assetsServersItem       { assetsMenu.find("li.menu-parent-assets-server-list")}
        assetsDatabasesItem     { assetsMenu.find("li.menu-parent-assets-database-list")}
        assetsStorageItem       { assetsMenu.find("li.menu-parent-assets-storage-list")}
        assetsLogicalItem       { assetsMenu.find("li.menu-parent-assets-storage-logical-list")}
        assetsDependItem        { assetsMenu.find("li.menu-parent-assets-dependencies-list")}
        assetsCommentsItem      { assetsMenu.find("li.menu-parent-assets-comments-list")}
        assetsDepAnalyzerItem   { assetsMenu.find("li.menu-parent-assets-dependency-analyzer")}
        assetsArchGraphItem     { assetsMenu.find("li.menu-parent-assets-architecture-graph")}
        assetsImportItem        { assetsMenu.find("li.menu-parent-assets-import-assets")}
        assetsManageBatchesItem { assetsMenu.find("li.menu-parent-assets-manage-batches")}
        assetsExportItem        { assetsMenu.find("li.menu-parent-assets-export-assets")}
        assetsViewManagerItem   { assetsMenu.find("li.menu-parent-assets-asset-manager")}

        tasksItem               { menuBar.find("li.menu-parent-tasks") }
        tasksMenu               { tasksItem.find("ul",class:"dropdown-menu menu-item-expand")}
        tasksSections           { tasksMenu.find("li",class:"menu-parent-item")}
        taskCount               { tasksMenu.find("span#todoCountProjectId")}
        tasksMyTasksItem        { tasksMenu.find("li.menu-parent-tasks-my-tasks")}
        tasksManagerItem        { tasksMenu.find("li.menu-parent-tasks-task-manager")}
        tasksGraphItem          { tasksMenu.find("li.menu-parent-tasks-task-graph")}
        tasksTimelineItem       { tasksMenu.find("li.menu-parent-tasks-task-timeline")}
        tasksCookbookItem       { tasksMenu.find("li.menu-parent-tasks-cookbook")}
        tasksGenHistoryItem     { tasksMenu.find("li.menu-parent-tasks-generation-history")}
        tasksImportItem         { tasksMenu.find("li.menu-parent-tasks-import-tasks")}

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

    def goToApplications(){
        selectMenu(assetsItem)
        selectItem(assetsApplicationsItem)
    }

    def goToTasksManager(){
        selectMenu(tasksItem)
        selectItem(tasksManagerItem)
    }

    def goToTasksCookbook(){
        selectMenu(tasksItem)
        selectItem(tasksCookbookItem)
    }
    def goToAssetViewManager(){
        selectMenu(assetsItem)
        selectItem(assetsViewManagerItem)
    }

// TODO Add remains menu items here


}
