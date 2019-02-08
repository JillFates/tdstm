package modules

import geb.Module


class ReportsMenuModule extends Module {

    static content = {
        menuContainer { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar { menuContainer.find("div#navbar-collapse")}

        reportsItem { menuBar.find("li.menu-parent-reports")}
        reportsMenu { reportsItem.find("ul",class:"dropdown-menu menu-item-expand")}
        reportsPages {reportsMenu.find("li.menu-child-item")}
        reportsApplicationProfileItem { reportsMenu.find("li.menu-reports-application-profiles")}
        reportsApplicationConflictsItem { reportsMenu.find("li.menu-reports-application-conflicts")}
        reportsServerConflictsItem { reportsMenu.find("li.menu-reports-server-conflicts")}
        reportsDatabaseConflictsItem { reportsMenu.find("li.menu-reports-database-conflicts")}
        reportsTaskReportItem { reportsMenu.find("li.menu-reports-task-report")}
        reportsActivityMetricsItem { reportsMenu.find("li.menu-reports-activity-metrics")}
        reportsApplicationEventResultsItem { reportsMenu.find("li.menu-reports-application-migration")}
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

    def goToReportsMenu(){
        reportsItem.click()
    }

    def goToApplicationProfiles(){
        selectMenu(reportsItem)
        selectItem(reportsApplicationProfileItem)
    }

    def goToApplicationConflicts(){
        selectMenu(reportsItem)
        selectItem(reportsApplicationConflictsItem)
    }

    def goToServerConflicts(){
        selectMenu(reportsItem)
        selectItem(reportsServerConflictsItem)
    }

    def goToDatabaseConflicts(){
        selectMenu(reportsItem)
        selectItem(reportsDatabaseConflictsItem)
    }

    def goToTaskReport(){
        selectMenu(reportsItem)
        selectItem(reportsTaskReportItem)
    }

    def goToActivityMetrics(){
        selectMenu(reportsItem)
        selectItem(reportsActivityMetricsItem)
    }

    def goToApplicationEventResults(){
        selectMenu(reportsItem)
        selectItem(reportsApplicationEventResultsItem)
    }
}
