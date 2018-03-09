package pages.AssetViewManager
import geb.Page


class ViewPage extends Page{

    static at = {
        waitFor {view.displayed}
        println (">>> method at,View Page")
    }
    static content = {
        view                (wait:true) { $("section","class":"page-asset-explorer-config")}
        clearBtn                    {$("button", id:"btnClear")}
    }

}
