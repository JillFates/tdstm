package pages.Assets

import geb.Page
import geb.error.RequiredPageContentNotPresent
import geb.Browser
import utils.CommonActions
import modules.CommonsModule
import modules.AssetsMenuModule

class AssetExportPage extends Page{

    static at = {
        sectionTitle.text() == "Export Assets"
        exportButton.isDisplayed()
    }

    static content = {
        commonsModule { module CommonsModule }
        assetsModule { module AssetsMenuModule}
        sectionTitle (wait:true){$("section.content-header h1")}
        exportButton {$("button", id:"exportButton")}
        bundleOptions {$("select#bundleId option")}
        assetOptions {$('li.list-group-item input[type=checkbox]')}
        assetCheckedOptions {$('li.list-group-item input[checked*=checked]')}
    }

    def randomSelectBundle(){
        // Planning bundle default selected, clean selection to random select
        browser.driver.executeScript('$("[value=useForPlanning]").removeAttr("selected")')
        def randomOption = CommonActions.getRandomOption bundleOptions
        randomOption.click()
    }

    def randomChooseItemsToExport(){
        try {
            // because remembers previous selection, cleans if needed.
            if (assetCheckedOptions.size() > 0) {
                CommonActions.uncheckCheckboxes assetCheckedOptions
            }
            randomCheckOptions()
        } catch (RequiredPageContentNotPresent e) {
            // assetCheckedOptions selector not found so no checked options
            randomCheckOptions()
        }
    }

    def randomCheckOptions(){
        // getting 3 random checkboxes to click because export process succeeds default timeout
        def randomElements = CommonActions.getRandomOptions assetOptions, 3
        randomElements.each { element ->
            element.click()
        }
    }

    def clickOnExportExcel(){
        exportButton.click()
    }
}