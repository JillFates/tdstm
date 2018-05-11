package pages.AssetViewManager
import geb.Page


class ViewPage extends Page{

    static at = {
        waitFor {view.displayed}
    }
    static content = {
        view              (wait:true) { $("section","class":"page-asset-explorer-config")}
        clearBtn          {$("button", id:"btnClear")}
        ViewMgrBreadCrumb {$('a.font-weight-bold')}
    }


    def clickViewManagerBreadCrumb(){
        ViewMgrBreadCrumb.click()
    }
}
