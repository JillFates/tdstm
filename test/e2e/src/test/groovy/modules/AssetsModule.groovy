package modules

import geb.Module

class AssetsModule extends Module {

    static content = {
        menuContainer { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar { menuContainer.find("div#navbar-collapse")}

        assetsItem { menuBar.find("li.menu-parent-assets")}
        assetsMenu { assetsItem.find("ul",class:"dropdown-menu menu-item-expand")}
        assetsPages {assetsMenu.find("li.menu-child-item")}
        assetsSections { assetsMenu.find("li",class:"menu-parent-item")}
        assetsSummaryItem { assetsMenu.find("li.menu-parent-assets-summary-table")}
        assetsAllAssetsItem { assetsMenu.find("li.menu-parent-assets-asset-explorer")}
        assetsApplicationsItem { assetsMenu.find("li.menu-parent-assets-application-list a[onclick*=hideMegaMenu]")}
        assetsDevicesItem { assetsMenu.find("li.menu-parent-assets-all-list a[onclick*=hideMegaMenu]")}
        assetsServersItem { assetsMenu.find("li.menu-parent-assets-server-list a[onclick*=hideMegaMenu]")}
        assetsDatabasesItem { assetsMenu.find("li.menu-parent-assets-database-list a[onclick*=hideMegaMenu]")}
        assetsStorageItem { assetsMenu.find("li.menu-parent-assets-storage-list a[onclick*=hideMegaMenu]")}
        assetsLogicalItem { assetsMenu.find("li.menu-parent-assets-storage-logical-list a[onclick*=hideMegaMenu]")}
        assetsDependItem { assetsMenu.find("li.menu-parent-assets-dependencies-list")}
        assetsCommentsItem { assetsMenu.find("li.menu-parent-assets-comments-list")}
        assetsDepAnalyzerItem { assetsMenu.find("li.menu-parent-assets-dependency-analyzer")}
        assetsArchGraphItem { assetsMenu.find("li.menu-parent-assets-architecture-graph")}
        assetsImportETLItem { assetsMenu.find("li.menu-parent-assets-import-assets-etl")}
        assetsImportExcelItem { assetsMenu.find("li.menu-parent-assets-import-assets")}
        assetsManageBatchesETLItem { assetsMenu.find("li.menu-parent-assets-manage-dep-batches")}
        assetsManageBatchesExcelItem { assetsMenu.find("li.menu-parent-assets-manage-batches")}
        assetsExportItem { assetsMenu.find("li.menu-parent-assets-export-assets")}
        assetsViewManagerItem { assetsMenu.find("li.menu-parent-assets-asset-manager")}

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

    def goToAssetsMenu() {
        assetsItem.click()
    }

    def goToSummaryTable(){
        selectMenu(assetsItem)
        selectItem(assetsSummaryItem)
    }

    def goToApplications(){
        selectMenu(assetsItem)
        selectItem(assetsApplicationsItem)
    }

    def goToDevices(){
        selectMenu(assetsItem)
        selectItem(assetsDevicesItem)
    }

    def goToServers(){
        selectMenu(assetsItem)
        selectItem(assetsServersItem)
    }

    def goToDatabases(){
        selectMenu(assetsItem)
        selectItem(assetsDatabasesItem)
    }

    def goToStorageDevices(){
        selectMenu(assetsItem)
        selectItem(assetsStorageItem)
    }

    def goToStorageLogical(){
        selectMenu(assetsItem)
        selectItem(assetsLogicalItem)
    }

    def goToDependencies(){
        selectMenu(assetsItem)
        selectItem(assetsDependItem)
    }

    def goToDependencyAnalyzer(){
        selectMenu(assetsItem)
        selectItem(assetsDepAnalyzerItem)
    }

    def goToArchitectureGraph(){
        selectMenu(assetsItem)
        selectItem(assetsArchGraphItem)
    }

    def goToComments(){
        selectMenu(assetsItem)
        selectItem(assetsCommentsItem)
    }

    def goToManageImportBatchETL(){
        selectMenu(assetsItem)
        selectItem(assetsManageBatchesETLItem)
    }

    def goToManageImportBatchExcel(){
        selectMenu(assetsItem)
        selectItem(assetsManageBatchesExcelItem)
    }

    def goToAssetViewManager(){
        selectMenu(assetsItem)
        selectItem(assetsViewManagerItem)
    }

    def goToAssetExport() {
        selectMenu (assetsItem)
        selectItem (assetsExportItem)
    }

    def goToAssetImportETL() {
        selectMenu (assetsItem)
        selectItem (assetsImportETLItem)
    }

    def goToAssetImportExcel() {
        selectMenu (assetsItem)
        selectItem (assetsImportExcelItem)
    }

    def goToAllAssets(){
        selectMenu(assetsItem)
        selectItem(assetsAllAssetsItem)
    }



}
