package pages.AssetViewManager
import geb.Page
import modules.MyViewsModule
import modules.CreateViewModule
import org.openqa.selenium.By

class AssetViewsPage extends Page{

    static at = {
        waitFor {viewMgrPageWindow.displayed}
        avPageTitle.text().trim().startsWith("View")
    }

    static content = {
        viewMgrPageWindow           (wait:true) { $("div","class":"col-md-2 asset-explorer-index-left-menu")}
        viewsMenu                   {viewMgrPageWindow.find("ul",class:"nav nav-pills nav-stacked")}
        viewOptions                 {viewMgrPageWindow.find("a")}
        viewMgrMyViews              {viewOptions[2]}
        viewSystemViews             {viewOptions[4]}
        //>>>>>>>>> MODULES
        viewsModule                 { module MyViewsModule}
        createViewModule            { module CreateViewModule}

        avPageTitle                 { $("section", 	class:"content-header").find("h1") }
        //>>>>>>>>>>BUTTONS
        createViewBton              {$("button", text:"Create View")}
        toggleListBtn               { staffViewHeaderBar.find("a", class:"ui-jqgrid-titlebar-close HeaderButton")}
        filter                      {$("input",placeholder: "Enter view name to filter")}
        //>>> grids
        previewGrid                 {$("kendo-grid",class:"k-widget k-grid k-grid-lockedcolumns")}
        vwGrid                      (required: false, wait:true){$("table", class:"table table-hover table-striped")}
        vwGridRows                  (required: false, wait:true) { vwGrid.find("tr","role":"row")}
    }
    def goToCreateView(){
        createViewBtn.click()
    }
    def goToMyViews(){
        viewMgrMyViews.click()
    }
    def filterViewByName(String name){
        filter=name
    }

    def openViewByName(name){
        filterViewByName name
        def link = $(By.xpath("//tr/td/a[text()='${name}']"))
        link.click()
    }
}
