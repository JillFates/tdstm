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
        nextButton.parent().@disabled == "true"
        cancelButton.displayed
        closeButton.displayed
    }
    static content = {
        bulkChangeEditAssetModal { $("#bulk-change-edit-component")}
        modalTitle { bulkChangeEditAssetModal.find(".modal-title")}
        nextButton { bulkChangeEditAssetModal.find("button span.glyphicon-step-forward")}
        cancelButton { bulkChangeEditAssetModal.find("button span.glyphicon-ban-circle")}
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