package pages.Assets.AssetViewManager

import geb.Page
import modules.CreateViewModule
import modules.ViewsModule
import modules.CommonsModule
import modules.MenuModule
import modules.AssetsMenuModule

class AssetViewsPage extends Page{
    static int favLimit=10

    static at = {
        waitFor {viewMgrPageWindow.displayed}
        avPageTitle.text().trim().startsWith("View")
        waitFor {allViewsModule.viewModuleContainer.displayed}

    }

    static content = {
        viewMgrPageWindow { $("div","class":"col-md-2 asset-explorer-index-left-menu")}
        viewsMenu {viewMgrPageWindow.find("ul",class:"nav nav-pills nav-stacked")}
        viewOptions {viewMgrPageWindow.find("a")}
        viewMgrAllViews {viewOptions[0]}
        viewMgrFavoriteViews {viewOptions[1]}
        viewMgrMyViews {viewOptions[2]}
        viewMgrSharedViews {viewOptions[3]}
        viewMgrSystemViews {viewOptions[4]}
        favViewsCounter(required: false, wait:true) {viewOptions[1].find("span")}
        sharedViewsCounter (required: false, wait:true) {viewOptions[3].find("span")}

        //>>>>>>>>> MODULES
        upperMenu { module MenuModule}
        allViewsModule { module ViewsModule}
        createViewModule { module CreateViewModule}
        common { module CommonsModule}
        assetsModule { module AssetsMenuModule }

        avPageTitle { $("h1")}
        //>>>>>>>>>>BUTTONS
        toggleListBtn { staffViewHeaderBar.find("a", class:"ui-jqgrid-titlebar-close HeaderButton")}

        //>>> grids
        previewGrid {$("kendo-grid",class:"k-widget k-grid k-grid-lockedcolumns")}
        vwGrid (required: false, wait:true){$("table", class:"table table-hover table-striped")}
        vwGridRows (required: false, wait:true) { vwGrid.find("tr","role":"row")}
        vwGridRowsLink {$("tr td a")}

        //pop ups
        favViewsLimitPopup (required: false, wait:true){$(".alert-dismissable")}
        closeLimitPopupBtn (required: false, wait:true){favViewsLimitPopup.find(".close")}
    }
    def goToCreateView(){
        createViewBtn.click()
    }
    def goToMyViews(){
        waitFor{viewMgrMyViews.click()}
    }
    def goToAllViews(){
        viewMgrAllViews.click()
    }

    def goToFavourites(){
        viewMgrFavoriteViews.click()
    }

    def goToSharedViews(){
        waitFor{viewMgrSharedViews.click()}
    }

    def goToSystemViews(){
        viewMgrSystemViews.click()
    }

    def getFavCounter(){
        waitFor{favViewsCounter.text().toInteger()}
    }

    def validateValueIncrement(int initial, int incremented){
        initial+1==incremented
    }

    /**
     * Adds fav views. On the attempt to add an 11th, the user
     * is to get a pop up
     */
    def getFavLimitPopUp(){
        def counter=getFavCounter()
        while (getFavCounter()<favLimit){
            allViewsModule.favRandomFavs()
            counter=getFavCounter()
        }
        allViewsModule.setFirstNonFavViewAsFav()
        favLimitPopUpIsPresent()
        favViewsCounter.text().toInteger()==favLimit
    }

    def favLimitPopUpIsPresent(){
        //Maximum number of favorite data views reached
        waitFor{favViewsLimitPopup.displayed}
    }
    def favLimitPopUpTextIsAsExpected(){
        //Maximum number of favorite data views reached
        favViewsLimitPopup.text().trim().contains("Maximum number of favorite data views reached")
    }

    def closeFavLimitPopUp(){
        closeLimitPopupBtn.click()
    }

    def validateSharedViewsCount(){
        getSharedCounter()==allViewsModule.getNumberOfRows()
    }

    def getSharedCounter(){
        sharedViewsCounter.text().toInteger()
    }
}
