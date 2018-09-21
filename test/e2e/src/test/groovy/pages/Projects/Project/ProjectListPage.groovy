package pages.Projects.Project

import geb.Page
import modules.CommonsModule
import modules.ProjectsModule
import geb.waiting.WaitTimeoutException

class ProjectListPage extends Page {

    static at = {
        projectPageTitle.text().trim() == "Project List - Active Projects"
        pageBreadcrumbs[0].text() == "Project"
        pageBreadcrumbs[1].text() == "Active"

        // TODO following item have the elements inside the label and cannot be reached
        // headerBarTitle == "Projects:"
        createProjectBtn.value() == "Create Project"
        showCompletedBtn.value() == "Show Completed Projects"
    }

    static content = {
        projectPageTitle (wait:true){ $("section", 	class:"content-header").find("h1")}
        pageBreadcrumbs             { $("ol", class:"breadcrumb").find("li a")}
        projectView                 { $("div#gview_projectGridIdGrid")}
        // TODO following item have the elements inside label

        //headerBarTitle          { $("span", class:"ui-jqgrid-title") }
        createProjectBtn(wait:true) { projectView.find("input", type: "button", class: "create", value: "Create Project")}
        showCompletedBtn            { projectView.find("input", type: "button", value: "Show Completed Projects")}
        toggleListBtn               { projectView.find("a", class:"ui-jqgrid-titlebar-close HeaderButton")}
        projectGridHeader           { projectView.find("div#ui-jqgrid-hbox")}
        columnsHeader               { projectGridHeader.find("tr", class: "ui-jqgrid-labels ui-sortable")}
        searchToolbar               { projectGridHeader.find("tr", class: "ui-search-toolbar")}
        projectGridHeaderCols       { columnsHeader.find("div", class:"ui-jqgrid-sortable")}
        projectGridRows             { projectView.find("table#projectGridIdGrid").find("tr.ui-widget-content")}
        gridSize                    { projectGridRows.size()}
        rowSize                     { projectGridHeaderCols.size()}
        projectNameFilter           { $("input#gs_projectCode")}
        projectGridPager            { $("div#pg_projectGridIdGridPager")}
        projectNameGridField        {$("td", "role": "gridcell", "aria-describedby": "projectGridIdGrid_projectCode")}
        projectDeletedMessage { $("#messageDivId")}
        showActiveBtn { projectView.find("input", type: "button", value: "Show Active Projects")}
        projectsModule { module ProjectsModule}
        commonsModule { module CommonsModule}
    }

    def clickOnCreateButton(){
        waitFor {createProjectBtn.click()}
    }

    def filterByName(name){
        waitFor {projectNameFilter.displayed}
        projectNameFilter = name
        commonsModule.waitForLoadingMessage()
    }

    def clickOnProjectByName(name){
        waitFor {projectNameGridField.find("a", text: contains(name)).first().click()}
    }

    def getListedProjectsSize(){
        waitFor{projectGridRows[0].displayed}
        projectGridRows.size()
    }

    def verifyRowsDisplayed(){
        try {
            waitFor(0.5){projectGridRows[0].displayed}
        } catch (WaitTimeoutException e){
            false
        }
    }

    def verifyDeletedMessage(){
        waitFor{projectDeletedMessage.displayed}
        projectDeletedMessage.text().contains("deleted")
    }

    def clickOnCompletedProjectsButton(){
        waitFor {showCompletedBtn.click()}
    }

    def clickOnActiveProjectsButton(){
        waitFor {showActiveBtn.click()}
    }
}
