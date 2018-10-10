package modules

import geb.Module

class PlanningModule extends Module {

    static content = {
        menuContainer { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar { menuContainer.find("div#navbar-collapse")}

        planningItem { menuBar.find("li.menu-parent-planning") }
        planningMenu  { planningItem.find("ul",class:"dropdown-menu menu-item-expand")}
        planningEventsTitle { planningMenu.find("li.menu-parent-item")[0]}
        planningListEvents { planningMenu.find("li.menu-parent-planning-event-list")}
        planningEventDetails { planningMenu.find("li.menu-parent-planning-event-detail-list")}
        planningListEventNews { planningMenu.find("li.menu-parent-planning-event-news")}
        planningPreEventCheckList { planningMenu.find("li.menu-child-item").find("a", "href": "/tdstm/reports/preMoveCheckList")}
        planningExportRunbook { planningMenu.find("li.menu-parent-planning-export-runbook")}
        planningBundlesTitle { planningMenu.find("li.menu-parent-item")[1]}
        planningListBundles { planningMenu.find("li.menu-parent-planning-list-bundles")}
        bundleName (required:false) {$("li.menu-parent-planning-selected-bundle")}
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

    def goToPlanningMenu() {
        planningItem.click()
    }

    def goToListEvents() {
        selectMenu(planningItem)
        selectItem(planningListEvents)
    }

    def goToEventDetails() {
        selectMenu(planningItem)
        selectItem(planningEventDetails)
    }

    def goToListEventNews() {
        selectMenu(planningItem)
        selectItem(planningListEventNews)
    }

    def goToPreEventChecklist() {
        selectMenu(planningItem)
        selectItem(planningPreEventCheckList)
    }

    def goToExportRunbook() {
        selectMenu(planningItem)
        selectItem(planningExportRunbook)
    }

    def goToListBundles() {
        selectMenu(planningItem)
        selectItem(planningListBundles)
    }

    def verifyEventsTitle(){
        planningEventsTitle.isDisplayed()
    }

    def verifyBundlesTitle(){
        planningBundlesTitle.isDisplayed()
    }

    def vaildateDisplayedBundleName(bdlName){
        bundleName.text().contains(bdlName)
    }
}
