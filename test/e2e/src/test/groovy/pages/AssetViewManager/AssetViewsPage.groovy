package pages.AssetViewManager
import geb.Page
import modules.CreateViewModule
import modules.ViewsModule

class AssetViewsPage extends Page{

    static at = {
        waitFor {viewMgrPageWindow.displayed}
        avPageTitle.text().trim().startsWith("View")
        waitFor {allViewsModule.viewModuleContainer.displayed}
    }

    static content = {
        viewMgrPageWindow           (wait:true) { $("div","class":"col-md-2 asset-explorer-index-left-menu")}
        viewsMenu                   {viewMgrPageWindow.find("ul",class:"nav nav-pills nav-stacked")}
        viewOptions                 {viewMgrPageWindow.find("a")}
        viewMgrAllViews             {viewOptions[0]}
        viewMgrFavoriteViews        {viewOptions[1]}
        viewMgrMyViews              {viewOptions[2]}
        viewMgrSharedViews          {viewOptions[3]}
        viewMgrSystemViews          {viewOptions[4]}
        favViewsCounter             (required: false, wait:true){viewOptions[1].find("span")}

        //>>>>>>>>> MODULES
        allViewsModule              { module ViewsModule}
        createViewModule            { module CreateViewModule}

        avPageTitle                 { $("h1")}
        //>>>>>>>>>>BUTTONS
        createViewBton              {$("button", text:"Create View")}
        toggleListBtn               { staffViewHeaderBar.find("a", class:"ui-jqgrid-titlebar-close HeaderButton")}

        //>>> grids
        previewGrid                 {$("kendo-grid",class:"k-widget k-grid k-grid-lockedcolumns")}
        vwGrid                      (required: false, wait:true){$("table", class:"table table-hover table-striped")}
        vwGridRows                  (required: false, wait:true) { vwGrid.find("tr","role":"row")}
        vwGridRowsLink {$("tr td a")}
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

    def goToSystemViews(){
        viewMgrSystemViews.click()
    }

    def getFavCounter(){
        favViewsCounter.text().toInteger()
    }

    def validateValueIncrement(int initial, int incremented){
        initial+1==incremented
    }
}
