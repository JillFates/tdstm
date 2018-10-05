package modules

import geb.Module
import pages.Tasks.*
import pages.Tasks.TaskManager.*
import pages.Cookbook.*

class TasksModule extends Module {

    static content = {
        menuContainer { $( "div", class:"container menu-top-container menu-top-container-full-menu")}
        menuBar { menuContainer.find("div#navbar-collapse")}

        tasksItem { menuBar.find("li.menu-parent-tasks")}
        tasksMenu { tasksItem.find("ul",class:"dropdown-menu menu-item-expand")}
        tasksPages {tasksMenu.find("li.menu-child-item")}
        tasksSections { tasksMenu.find("li",class:"menu-parent-item")}
        taskCount { tasksMenu.find("span#todoCountProjectId")}
        tasksMyTasksItem { tasksMenu.find("li.menu-parent-tasks-my-tasks")}
        tasksManagerItem { tasksMenu.find("li.menu-parent-tasks-task-manager")}
        tasksGraphItem { tasksMenu.find("li.menu-parent-tasks-task-graph")}
        tasksTimelineItem { tasksMenu.find("li.menu-parent-tasks-task-timeline")}
        tasksCookbookItem { tasksMenu.find("li.menu-parent-tasks-cookbook")}
        tasksGenHistoryItem { tasksMenu.find("li.menu-parent-tasks-generation-history")}
        tasksImportItem { tasksMenu.find("li.menu-parent-tasks-import-tasks")}
        projectName { menuContainer.find("li a#nav-project-name")}

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

    def goToTasksMenu(){
        tasksItem.click()
    }

    def goToMyTasks(){
        selectMenu(tasksItem)
        selectItem(tasksMyTasksItem)
    }

    def goToTasksManager(){
        selectMenu(tasksItem)
        selectItem(tasksManagerItem)
    }

    def goToTaskGraph(){
        selectMenu(tasksItem)
        selectItem(tasksGraphItem)
    }

    def goToTaskTimeline(){
        selectMenu(tasksItem)
        selectItem(tasksTimelineItem)
    }

    def goToTasksCookbook(){
        selectMenu(tasksItem)
        selectItem(tasksCookbookItem)
    }

    def goToGenerationHistory(){
        selectMenu(tasksItem)
        selectItem(tasksGenHistoryItem)
    }

    def goToImportTasks(){
        selectMenu(tasksItem)
        selectItem(tasksImportItem)
    }

    def assertProjectName(name){
        projectName.text().contains(name)
    }
}
