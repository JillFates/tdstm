package pages.Projects.Project

import geb.Page
import modules.CommonsModule
import modules.ProjectsMenuModule
import geb.waiting.WaitTimeoutException

class ProjectListPage extends Page {

    static at = {
        projectPageTitle.text() == "Projects - Active"
        pageBreadcrumbs[0].text() == "Projects"
        pageBreadcrumbs[1].text() == "Active"

        // TODO following item have the elements inside the label and cannot be reached
        // headerBarTitle == "Projects:"
    }

    static content = {
        projectPageTitle (wait:true){ $("section", 	class:"content-header").find("h2")}
        pageBreadcrumbs             { $("ol", class:"breadcrumb-container").find("li")}
        // TODO: We have to refactor the whole projectView thing because everything changed
        //projectView                 { $("div#gview_projectGridIdGrid")}
        //headerBarTitle          { $("span", class:"ui-jqgrid-title") }
        createProjectBtn(wait:true) {$(type: "button", title: "Create Projects")}
        showCompletedBtn            {$ ("div", class:"project-active-completed-options"). find("input", name:"active")}
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
        projectNameColumn  {$("td", "role": "gridcell", "aria-describedby": "projectGridIdGrid_name")}
        projectDeletedMessage { $("#messageDivId")}
        showActiveBtn { projectView.find("input", type: "button", value: "Show Active Projects")}
        projectsModule { module ProjectsMenuModule}
        commonsModule { module CommonsModule}
        noRecords (required: false) {$(".ui-paging-info", text:'No records to view')}
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
    /**
     * Validates the "No Records" message is displayed
     */
    def noRecrdsAreDisplayed(){
        noRecords.displayed
    }

    def clickOnFirstListedProject(){
        waitFor {projectNameGridField[0].find("a").click()}
    }

    def getFirstProjectName(){
        projectNameColumn[0].text()
    }
    def getFirstProjectCode(){
        projectNameGridField[0].text()
    }
}
