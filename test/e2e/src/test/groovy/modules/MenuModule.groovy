package modules

import geb.Module

/**
 * This class represents the USER MENU SECTION in the upper right part of the application
 */

class MenuModule extends Module {

    static content = {
        menuContainer       { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar             { menuContainer.find("div#navbar-collapse")}
        projectName { menuContainer.find("li a#nav-project-name")}
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

    def assertProjectName(name){
        projectName.text().contains(name)
    }

    def assertBundleName(bdlName){
        projectName.text().contains(bdlName)
    }
}
