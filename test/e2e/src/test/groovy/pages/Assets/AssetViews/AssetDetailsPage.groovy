package pages.Assets.AssetViews
import geb.Page

/**
 * This class represents the new generic asset details
 * page
 * @author ingrid
 */
class AssetDetailsPage extends Page{
    static at = {
        assetDetailModal.displayed
        modalTitle.text().contains("Detail")
        editButton.present
        closeButton.present
    }
    static content = {
        assetDetailModal { $("div.tds-angular-component-content")}
        modalTitle { assetDetailModal.find(".modal-title")}
        editButton { assetDetailModal.find("button span.glyphicon-pencil")}
        closeButton { assetDetailModal.find("button span.glyphicon-ban-circle")}

        adModalAssetName {$('td.label.assetName').next()}
        adModalLastUpdated {$(".last-updated")}
    }

    def validateDataIsPresent(List rowData, List dataDisplayed){
        def success=false
        rowData.each { data ->
            if(data != "") {
                if(dataDisplayed.find {it.contains(data)}){
                    success = true
                }
                assert success, "$data was not found in the details page."
            }
        }
        true
    }

    def getContent(){
        def screenData =$(".valueNW")
        def screenText=[]
        screenText.add($('td.label.assetName.O').next().text())
        screenData.each{
            screenText.add(it.text().trim())
        }
        screenText.add(getLastUpdated())
        screenText
    }
    def getLastUpdated(){
        adModalLastUpdated.text().split(" ")[2]+ (" ")+adModalLastUpdated.text().split(" ")[3]+ (" ")+adModalLastUpdated.text().split(" ")[4]
    }
    def getName(){
        adModalAssetName.text()
    }
}