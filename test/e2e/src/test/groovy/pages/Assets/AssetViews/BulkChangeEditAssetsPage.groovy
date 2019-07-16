package pages.Assets.AssetViews

/**
 * @author Sebastian Bigatton
 */

import geb.Page
import modules.CommonsModule

class BulkChangeEditAssetsPage extends Page{
    static at = {
        bulkChangeEditAssetModal.displayed
        modalTitle.text() == "Bulk Change > Edit Assets"
        nextButton.@disabled == "true"
        cancelButton.displayed
        closeButton.displayed
    }
    static content = {
        bulkChangeEditAssetModal { $("#bulk-change-edit-component")}
        modalTitle { bulkChangeEditAssetModal.find(".modal-title")}
        nextButton { bulkChangeEditAssetModal.find("button", title:'Next')}
        cancelButton { bulkChangeEditAssetModal.find("tds-button-cancel")}
        closeButton { bulkChangeEditAssetModal.find("button.close")}
        fieldRows { bulkChangeEditAssetModal.find("[kendogridlogicalrow]")}
        fieldName { fieldRows.find("kendo-dropdownlist", "name": "field")}
        action { fieldRows.find("kendo-dropdownlist", "name": "action")}
        valueOpen { value.find(".component-action-open")}
        commonsModule { module CommonsModule}
    }

    def selectActionByText(text){
        waitFor{action.click()}
        commonsModule.selectKendoDropdownOptionByText(text)
    }

    def selectFieldNameByText(text){
        waitFor{fieldName.click()}
        commonsModule.selectKendoDropdownOptionByText(text)
    }

    def clickOnNextButton(){
        waitFor{nextButton.click()}
    }
}