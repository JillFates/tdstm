package pages.Assets.AssetViews

import geb.Page

/**
 * This class represents the new generic asset details
 * page
 * @author Sebastian
 */
class AssetCreatePage extends Page{
    static at = {
        assetCreateModal.displayed
        modalTitle.text().contains("Create")
        createButton.present
        cancelButton.present
    }
    static content = {
        assetCreateModal { $("div.tds-angular-component-content")}
        modalTitle { assetCreateModal.find(".modal-title")}
        createButton { assetCreateModal.find("button.component-action-update")}
        cancelButton { assetCreateModal.find("button.component-action-cancel")}

        acModalSelectorValues(wait:true, required:false) { $("kendo-popup kendo-list li[role=option]")}
        acModalAppName { assetCreateModal.find("input#assetName")}
        acModalDescription { assetCreateModal.find("input#description")}
        acModalSME1Selector { assetCreateModal.find("td[data-for=sme] span.k-select")}
        acModalSME2Selector { assetCreateModal.find("td[data-for=sme2] span.k-select")}
        acModalAppOwnerSelector(wait:true) { assetCreateModal.find("td[data-for=appOwner] span.k-select")}
        acModalBundleSelector { assetCreateModal.find("td[data-for=moveBundle] span.k-select")}
        acModalPlanStatusSelector { assetCreateModal.find("td[data-for=planStatus] span.k-select")}
    }

    def createApplication(dataMap){
        waitFor {acModalAppName.displayed}
        acModalAppName = dataMap.appName
        acModalDescription = dataMap.appDesc

        acModalSME1Selector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        acModalSelectorValues[2].click()

        acModalSME2Selector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        acModalSelectorValues[2].click()

        acModalAppOwnerSelector.click()
        waitFor { acModalSelectorValues.size() > 2 }
        acModalSelectorValues.last().click()

        waitFor{ acModalBundleSelector.click()}
        acModalSelectorValues.find(text: dataMap.appBundle).click()
        acModalBundleSelector.click() // close dropdown

        waitFor{ acModalPlanStatusSelector.click()}
        acModalSelectorValues.find(text: dataMap.appStatus).click()

        createButton.click()
    }
}