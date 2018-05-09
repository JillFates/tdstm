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
        viewsListed       {$("[uisref]")}
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
    def listedViews(){
        def rows = viewsListed
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
        def validation=(sharedViews.size()==vwGridRows.size())
    }
    /**
     * System Views will have no author (created by field empty)
     */
    def systemViewsOnly(){
        createdBy.text()==""
    }
    def validateViewIsListed(String viewName){
        listedViews().find {
            if (it.text()==viewName) return true
            return false
        }
    }
}
