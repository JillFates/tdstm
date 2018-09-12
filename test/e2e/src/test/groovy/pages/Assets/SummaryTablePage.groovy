package pages.Assets
import geb.Page
import modules.AssetsModule
import modules.CommonsModule

class SummaryTablePage extends Page {

    static at = {
        modaltitle.text().contains("Asset Summary Table")
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")[0]}
        assetsModule { module AssetsModule}
        commonsModule { module CommonsModule }
        justPlanningCheckbox {$("input",  id:"justPlanning")}

    }


}