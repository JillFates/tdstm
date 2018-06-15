package modules

import geb.Module
import org.openqa.selenium.Keys

class ViewsModule extends Module {

    static content = {
        moduleTitle       {$(".box-title.box-title-grid-list")}
        viewsContainer    { $( "div", class:"content body")}
        viewModuleContainer {viewsContainer.find(".asset-explorer-index-container")}

        filter            {$("input",placeholder: "Enter view name to filter")}
        clearFilterX      {$(".form-control-feedback")}
        viewList          { $( "div", class:"table-responsive").find("tbody")}
        viewListTableBody {$("tbody")}
        viewsListed       {$("[uisref]")}
        viewsContainer    { $( "div", class:"content body")}
        viewList          { $( "div", class:"table-responsive").find("tbody")}
        vwGrid            (required: false, wait:true){$("table", class:"table table-hover table-striped")}
        vwGridRows        (required: false, wait:true) { vwGrid.find("tbody tr")}

        deleteButtons     {viewList.find("title": "Click to delete this view")}
        editButtons       {viewList.find("title": "Click to edit this view")}
        createViewButton  {viewsContainer.find("button", text:containsWord("Create"))}
        closeDeleteModal  {$("button.close")}
        createViewButton  {viewsContainer.find("button", text:containsWord("Create"))}

        voidStars         (required: false) {$("div.table-responsive i.fa.text-yellow.fa-star-o")}
        yellowStars       (required: false) {$("div.table-responsive i.fa.text-yellow.fa-star")}
        createdBy         {viewList.find("td:nth-child(4)")}
        ticks              {viewList.find(".glyphicon-ok")}// unchecked views will not have a span

    }

    def openRandomView(){
        def willSelect =Math.abs(new Random().nextInt() % viewsListed.size())+1
        def editedViewName = viewsListed[willSelect].text()
        waitFor{viewsListed[willSelect].click()}
        editedViewName
    }

    def clickCreateView(){
        createViewButton.click()
    }
    def moduleTitleIsCorrect(String title){
        moduleTitle.text()==title
    }
    def validateAuthor(){
        createdBy.each{
            def validation=true
            if(it.text()!="e2e user"){
                validation=false
            }
        }
    }
    def clickOnFirstDelete(){
        deleteButtons[0].click()
    }
    def confirmationRequiredIsDisplayed(){
        waitFor{($("h4.modal-title")).isDisplayed()}
        closeDeleteModal.click()
    }
    def closeDeleteModal(){
        closeDeleteModal.click()
    }
    /**
     * Checks that no star is  void, since this is  the FAVS section
     * returns false if a void star is displayed.
     */
    def noVoidStarsAreDisplayed(){
        !voidStars.displayed
    }
    /**
     * Shared views will have a tick on the "Shared" column
     * In This section the number of rows should be the same as number of ticks.
     * This method validates the the condition of equal number of ticks and rows is met.
     *
     */
    def validateIsShared(){
        ticks.size()==vwGridRows.size()
    }
    /**
     * System Views views will have a tick on the "System" column
     * In This section the number of rows should be the same as number of ticks.
     * This method validates the the condition of equal number of ticks and rows is met.
     */
    def systemViewsOnly(){
        // createdBy.text()==""
        ticks.size()==viewsListed.size()
    }
    /**
     * This method validates how the mame of a view is listed in a grid.
     * If the parameter isListed receives TRUE the method will check whether
     * a view name is LISTED at least once in the grid, while receiving FALSE will have the
     * method to check whether each view name CONTAINS the text in the viewName parameter.
     * @param viewName
     * @param isListed
     * @return
     */
    def validateRowNames(String viewName, boolean isListed){
        def found=false
        def element
        def elements
        if(isListed){
            element=viewsListed.find{el -> el.text()==viewName}
            if(element) found = true
        }else{
            elements=viewsListed.findAll{els -> els.text().contains(viewName)}
            if(elements.size()==viewsListed.size())
                found = true
        }
        found
    }
    /**
     * Clicks on the first row
     * @return
     */
    def clickFirstViewOfTheList(){
        vwGrid.find("tr")[1].find("a")[1].click()
    }

    def filterViewByName(String name){
        filter=name
    }

    def clearFilterViewByName(){
        clearFilterX.click()
    }
    /**
     * Receives a number of ros that had been saved before in the spec and compares it ti the
     * curren number of rows
     * @param numberOfRows
     * @return
     */
    def allRowsAreBack(int numberOfRows){
        getNumberOfRows()==numberOfRows
    }

    def getNumberOfRows(){
        viewsListed.size()
    }

    def openViewByName(name){
        filterViewByName name
        // verify exact match and no other was found with same name
        // otherwise we can click in other view than is required
        def links = viewsListed.findAll { it.text() == name }
        links.size() == 1
        waitFor{ links[0].click() }
    }

    def numberViewNamesEqualsNumberRows(){
        vwGridRows.size()==viewsListed.size()
    }

    def numberEditButtonsEqualsNumberRows(){
        editButtons.size()==vwGridRows.size()
    }

    def createdDateNotEmpty(){
        vwGridRows.findAll { it.find{"td[4]"}.text()!="" }
    }

    def setFirstNonFavViewAsFav(){
        voidStars[0].click()
    }

    def goToFirstNonFavView(){
        voidStars[0].parent().parent().next().find("[uisref]").click()
    }

    def getNameOfFirstNonFavView(){
        voidStars[0].parent().parent().next().text()
    }
}
