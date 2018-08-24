package modules

import geb.Module

class ProjectsModule extends Module {

    static at = {

        projectsItem.text().trim()          == "Projects"
        projectsActiveItem.text().trim()    == "Active Projects"
        projectsCurrentItem.text().contains() == "Details" // TODO add the project name as test property
        projectsMailItem.text().trim()      == "User Activation Emails"
        projectsStaffItem.text().trim()     == "Project Staff"
        projectsFieldsSetItem.text().trim() == "Asset Field Settings"

    }

    static content = {
        menuContainer { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar { menuContainer.find("div#navbar-collapse")}

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
        projectsDatascripts { projectsMenu.find("li.menu-child-item").find("a", "href": "/tdstm/module/datascript/list")}
        projectsActions { projectsMenu.find("li.menu-child-item").find("a", "href": "/tdstm/module/action/list")}
        projectsTags { projectsMenu.find("li.menu-projects-tags")}
        projectName { menuContainer.find("li a#nav-project-name")}
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
        projectsItem.click()
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
        selectMenu(projectsItem)
        selectItem(projectsFieldsSetItem)
    }

    def goToProviders(){
        selectMenu(projectsItem)
        selectItem(projectsProviders)
    }

    def goToCredentials(){
        selectMenu(projectsItem)
        selectItem(projectsCredentials)
    }

    def goToDatascripts(){
        selectMenu(projectsItem)
        selectItem(projectsDatascripts)
    }

    def goToTagsPage(){
        selectMenu projectsItem
        selectItem projectsTags
    }

    def goToProjectsStaff(){
        selectMenu projectsItem
        selectItem projectsStaffItem
    }

    def assertProjectName(name){
        projectName.text().contains(name)
    }

}
