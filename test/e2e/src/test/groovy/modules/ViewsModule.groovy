package modules

import geb.Module

class ViewsModule extends Module {

    static content = {
        moduleTitle       {$(".box-title.box-title-grid-list")}
        viewsContainer    { $( "div", class:"content body")}

        viewList          { $( "div", class:"table-responsive").find("tbody")}
        viewListTableBody {$("tbody")}
        viewsListed       {$("[uisref]")}
        viewsContainer    { $( "div", class:"content body")}
        viewList          { $( "div", class:"table-responsive").find("tbody")}
        vwGrid            (required: false, wait:true){$("table", class:"table table-hover table-striped")}
        vwGridRows        (required: false, wait:true) { vwGrid.find("tbody tr")}


        deleteButtons     {viewList.find("title": "Click to delete this view")}
        createViewButton  {viewsContainer.find("button", text:containsWord("Create"))}
        closeDeleteModal  {$("button.close")}
        createViewButton  {viewsContainer.find("button", text:containsWord("Create"))}

        voidStars         (required: false) {$("div.table-responsive i.fa.text-yellow.fa-star-o")}
        yellowStars       (required: false) {$("div.table-responsive i.fa.text-yellow.fa-star")}
        createdBy         {viewList.find("td:nth-child(4)")}
        sharedViews       {viewList.find("tr span.glyphicon")}// unchecked views will not have a span

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
     * Shared views willhave a tick on the "Shared" column
     * In This section the number of rows should be the same as number of ticks.
     * This method validates the the condition of equal number of ticks and rows is met
     * and it assumes that there will always be at least one shared view,which is the all assets view.
     *
     */
    def validateIsShared(){
        sharedViews.size()==vwGridRows.size()
    }
    /**
     * System Views will have no author (created by field empty)
     */
    def systemViewsOnly(){
        createdBy.text()==""
    }
    /**
     * This validates that a view is listed
     * @param viewName
     * @return
     */
    def validateViewIsListed(String viewName){
        viewsListed.find {
            if (it.text()==viewName) return true
            return false
        }
    }
    /**
     * Here, we validata that ALL of th rows contain the text, therefore
     * the filtering was successfull
     * @param text
     * @return
     */
    def validateFilteredList(String text){
        boolean validList=true
        for (view in viewsListed){
           println(view.text())
           if(!view.text().contains(text)){
               validList=false
           }
        }
        validList
    }
    /**
     * This method validates how the mame of a view is listed in a grid.
     * If the parameter isListed receives TRUE the method will check whether
     * a view name is LISTED at lest once in the grid, while receiving FALSE will have the
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
}
