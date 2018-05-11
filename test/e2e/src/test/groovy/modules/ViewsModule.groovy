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
     * number of rows should be the same as number of ticks.
     */
    def validateIsShared(){
        if(sharedViews.size()>0){
            sharedViews.size()==vwGridRows.size()
        }else{
            println("No Shared views are displayed")
        }

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
    def clickFirstViewOfTheList(){
        vwGrid.find("tr")[1].find("a")[1].click()
    }
}
