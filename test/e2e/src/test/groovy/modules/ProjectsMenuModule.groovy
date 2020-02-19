package modules

import geb.Module

class ProjectsMenuModule extends Module {

    static content = {
        menuContainer { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar { menuContainer.find("div#mobile-nav") }

        projectsItem { menuBar.find("li.menu-parent-projects") }
        projectsMenu  { projectsItem.find("ul",class:"dropdown-menu menu-item-expand")}
        projectsPages {projectsMenu.find("li.menu-child-item")}
        projectsActiveItem { projectsMenu.find("li.menu-projects-active-projects")}
        projectsCurrentItem  { projectsMenu.find("li.menu-projects-current-project")}
        projectsStaffItem { projectsMenu.find("li.menu-projects-project-staff")}
        projectsMailItem { projectsMenu.find("li.menu-projects-user-activation")}
        projectsFieldsSetItem { projectsMenu.find("li.menu-projects-field-settings")}
        projectsProviders { projectsMenu.find("li.menu-child-item").find("a", "href": "/tdstm/module/provider/list")}
        projectsCredentials { projectsMenu.find("li.menu-child-item").find("a", "href": "/tdstm/module/credential/list")}
        projectsETLScripts { projectsMenu.find("li.menu-child-item").find("a", "href": "/tdstm/module/datascript/list")}
        projectsActions { projectsMenu.find("li.menu-child-item").find("a", "href": "/tdstm/module/action/list")}
        projectsTags { projectsMenu.find("li.menu-projects-tags")}
        projectName { menuContainer.find("span#nav-project-name").find("span")}
        projectLicenseIcon { menuContainer.find("li a.licensing-error-warning i.fa-warning")}

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

    def goToProjectsMenu() {
        waitFor{projectsItem.click()}
        waitFor(30){projectsItem.click()}
    }

    def goToProjectsActive(){
        selectMenu(projectsItem)
        selectItem(projectsActiveItem)
    }

    def goToProjectsDetails(){
        selectMenu(projectsItem)
        selectItem(projectsCurrentItem)
    }

    def goToUserActivationEmails(){
        selectMenu(projectsItem)
        selectItem(projectsMailItem)
    }

    def goToAssetFieldSettings(){
        waitFor(30){projectsItem}
        selectMenu(projectsItem)
        waitFor(30){projectsFieldsSetItem}
        selectItem(projectsFieldsSetItem)
    }

    def goToProviders(){
        waitFor (30) {projectsItem}
        selectMenu(projectsItem)
        waitFor (30) {projectsProviders}
        selectItem(projectsProviders)
    }

    def goToCredentials(){
        selectMenu(projectsItem)
        selectItem(projectsCredentials)
    }

    def goToETLScripts(){
        selectMenu(projectsItem)
        selectItem(projectsETLScripts)
    }

    def goToActions(){
        selectMenu(projectsItem)
        selectItem(projectsActions)
    }

    def goToTagsPage(){
        waitFor(30){projectsItem}
        selectMenu projectsItem
        waitFor(30) {projectsTags}
        selectItem projectsTags
    }

    def goToProjectsStaff(){
        selectMprojectNameenu projectsItem
        selectItem projectsStaffItem
    }

    def assertProjectName(name){
        projectName.text().contains(name)
    }

}
