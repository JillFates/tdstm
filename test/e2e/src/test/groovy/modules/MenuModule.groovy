package modules

import geb.Module

class MenuModule extends Module {

    static at = {

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
        menuBar             { menuContainer.find("div#navbar-collapse")}

        assetsItem              { menuBar.find("li.menu-parent-assets")}
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

        tasksItem               { menuBar.find("li.menu-parent-tasks")}
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

        userMenu {$(class:"user-menu")}
        logoutBtn {userMenu.find(class:"pull-right")}

    }

    def clickUserMenu(){
        userMenu.click()
    }

    def logout(){
        clickUserMenu()
        waitFor{logoutBtn.click()}
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

    def goToAssetExport() {
        selectMenu assetsItem
        selectItem assetsExportItem
    }

    def goToAllAssets(){
        selectMenu(assetsItem)
        selectItem(assetsAllAssetsItem)
    }



}
