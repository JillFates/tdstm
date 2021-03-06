package modules

import geb.Module

class PlanningMenuModule extends Module {

    static content = {
        menuContainer { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar { menuContainer.find("div#mobile-nav") }

        planningItem { menuBar.find("li.menu-parent-planning") }
        planningMenu  { planningItem.find("ul",class:"dropdown-menu menu-item-expand")}
        planningListEvents { planningMenu.find("li.menu-parent-planning-event-list")}
        planningEventDetails { planningMenu.find("li.menu-parent-planning-event-detail-list")}
        planningListEventNews { planningMenu.find("li.menu-parent-planning-event-news")}
        planningPreEventCheckList { planningMenu.find("li.menu-child-item").find("a", "href": "/tdstm/module/reports/preEventCheckList")}
        planningExportRunbook { planningMenu.find("li.menu-parent-planning-export-runbook")}
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
        waitFor(30) {planningItem.click()}
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
        waitFor(30){planningItem}
        selectMenu(planningItem)
        waitFor(30){planningListBundles}
        selectItem(planningListBundles)
    }

    def vaildateDisplayedBundleName(bdlName){
        bundleName.text().contains(bdlName)
    }
}
