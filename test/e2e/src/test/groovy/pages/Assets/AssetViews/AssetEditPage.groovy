package pages.Assets.AssetViews

/**
 * This class represents the new generic asset edit modal
 * page
 * @author Sebastian Bigatton
 */
import geb.Page
import modules.CommonsModule
import utils.CommonActions
import geb.waiting.WaitTimeoutException

class AssetEditPage extends Page {

    static at = {
        assetEditModal.displayed
        modalTitle.text().contains("Edit")
    }

    static content = {
        assetEditModal { $("div.tds-angular-component-content") }
        modalTitle { assetEditModal.find(".modal-title") }
        updateButton { assetEditModal.find("button", text: contains("Update")) }
        cancelButton { assetEditModal.find("button", text: contains("Cancel")) }
        closeButton { assetEditModal.find("button", class:"component-action-close")}
        deleteButton { assetEditModal.find("button", text: contains("Delete")) }
        assetTagSelector { assetEditModal.find("tds-asset-tag-selector") }
        tagsDropDown { assetTagSelector.find(".component-action-open") }
        tagsOptionList { $("div.k-list-container").find("li.k-item") }
        tagNamesSelector { tagsOptionList.find("div.asset-tag-selector-single-item") }
        deleteTagIcon { assetTagSelector.find("li span.k-i-close") }
        tagsInput { assetTagSelector.find("input#asset-tag-selector-component") }
        tagsSelected { assetTagSelector.find("kendo-taglist li") }
        commonsModule { module CommonsModule }

        aeModalSelectorValues(wait:true, required:false) { $("kendo-popup kendo-list li[role=option]")}

        aeModalAppName { assetEditModal.find("input#assetName")}
        aeModalDescription { assetEditModal.find("input#description")}
        aeModalSME1Selector { assetEditModal.find("td[data-for=sme]")}
        aeModalSME1Arrow { aeModalSME1Selector.find("span.k-select")}
        aeModalSME1Value { aeModalSME1Selector.find("span.k-input")}
        aeModalSME2Selector { assetEditModal.find("td[data-for=sme2]")}
        aeModalSME2Arrow { aeModalSME2Selector.find("span.k-select")}
        aeModalSME2Value { aeModalSME2Selector.find("span.k-input")}
        aeModalAppOwnerSelector(wait:true) { assetEditModal.find("td[data-for=appOwner]")}
        aeModalAppOwnerArrow { aeModalAppOwnerSelector.find("span.k-select")}
        aeModalAppOwnerValue { aeModalAppOwnerSelector.find("span.k-input")}
        aeModalBundleSelector { assetEditModal.find("td[data-for=moveBundle]")}
        aeModalBundleArrow { aeModalBundleSelector.find("span.k-select")}
        aeModalBundleValue { aeModalBundleSelector.find("span.k-input")}
        aeModalPlanStatusSelector { assetEditModal.find("td[data-for=planStatus]")}
        aeModalPlanStatusArrow { aeModalPlanStatusSelector.find("span.k-select")}
        aeModalPlanStatusValue { aeModalPlanStatusSelector.find("span.k-input")}

        aeModalValidationSelector       { assetEditModal.find("select#criticality")}
        aeModalStartUpSelector          { assetEditModal.find("div#s2id_startupById")}
        aeModalSupportsTable            { assetEditModal.find("kendo-grid", 0)}
        aeModalDependencyTable          { assetEditModal.find("kendo-grid", 1)}

        aeModalAddSuppBtn { aeModalSupportsTable.find("button.btn-add-new-dependency")}
        aeModalSuppColTitles (wait:true, required:false) { aeModalSupportsTable.find(".k-grid-header table thead th a label")}
        aeModalSuppList  { aeModalSupportsTable.find("kendo-grid-list tr")}

        aeModalSuppFreqSelector  { aeModalSuppList.first().find("td", "aria-colindex":"2")}
        aeModalSuppFreqArrow  { aeModalSuppFreqSelector.find("span.k-select")}
        aeModalSuppFreqValue  { aeModalSuppFreqSelector.find("span.k-input")}

        aeModalSuppClassSelector  { aeModalSuppList.first().find("td", "aria-colindex":"3")}
        aeModalSuppClassArrow  { aeModalSuppClassSelector.find("span.k-select")}
        aeModalSuppClassValue  { aeModalSuppClassSelector.find("span.k-input")}

        aeModalSuppNameSelector  { aeModalSuppList.first().find("td", "aria-colindex":"4")}
        aeModalSuppNameArrow  { aeModalSuppNameSelector.find("span.k-select")}
        aeModalSuppNameValue  { aeModalSuppNameSelector.find("kendo-searchbar", 0).find("input")}

        aeModalSuppBundleSelector  { aeModalSuppList.first().find("td", "aria-colindex":"5")}
        aeModalSuppBundleArrow  { aeModalSuppBundleSelector.find("span.k-select")}
        aeModalSuppBundleValue  { aeModalSuppBundleSelector.find("span.k-input")}

        aeModalSuppTypeSelector  { aeModalSuppList.first().find("td", "aria-colindex":"6")}
        aeModalSuppTypeArrow  { aeModalSuppTypeSelector.find("span.k-select")}
        aeModalSuppTypeValue  { aeModalSuppTypeSelector.find("span.k-input")}

        aeModalSuppStatusSelector  { aeModalSuppList.first().find("td", "aria-colindex":"7")}
        aeModalSuppStatusArrow  { aeModalSuppStatusSelector.find("span.k-select")}
        aeModalSuppStatusValue  { aeModalSuppStatusSelector.find("span.k-input")}

        aeModalAddIsDepBtn { aeModalDependencyTable.find("button.btn-add-new-dependency")}
        aeModalIsDepColTitles (wait:true, required:false) { aeModalSupportsTable.find(".k-grid-header table thead th a label")}
        aeModalIsDepList  { aeModalDependencyTable.find("kendo-grid-list tr")}

        aeModalIsDepFreqSelector  { aeModalIsDepList.first().find("td", "aria-colindex":"2")}
        aeModalIsDepFreqArrow  { aeModalIsDepFreqSelector.find("span.k-select")}
        aeModalIsDepFreqValue  { aeModalIsDepFreqSelector.find("span.k-input")}

        aeModalIsDepClassSelector  { aeModalIsDepList.first().find("td", "aria-colindex":"3")}
        aeModalIsDepClassArrow  { aeModalIsDepClassSelector.find("span.k-select")}
        aeModalIsDepClassValue  { aeModalIsDepClassSelector.find("span.k-input")}

        aeModalIsDepNameSelector  { aeModalIsDepList.first().find("td", "aria-colindex":"4")}
        aeModalIsDepNameArrow  { aeModalIsDepNameSelector.find("span.k-select")}
        aeModalIsDepNameValue  { aeModalIsDepNameSelector.find("kendo-searchbar", 0).find("input")}

        aeModalIsDepBundleSelector  { aeModalIsDepList.first().find("td", "aria-colindex":"5")}
        aeModalIsDepBundleArrow  { aeModalIsDepBundleSelector.find("span.k-select")}
        aeModalIsDepBundleValue  { aeModalIsDepBundleSelector.find("span.k-input")}

        aeModalIsDepTypeSelector  { aeModalIsDepList.first().find("td", "aria-colindex":"6")}
        aeModalIsDepTypeArrow  { aeModalIsDepTypeSelector.find("span.k-select")}
        aeModalIsDepTypeValue  { aeModalIsDepTypeSelector.find("span.k-input")}

        aeModalIsDepStatusSelector  { aeModalIsDepList.first().find("td", "aria-colindex":"7")}
        aeModalIsDepStatusArrow  { aeModalIsDepStatusSelector.find("span.k-select")}
        aeModalIsDepStatusValue  { aeModalIsDepStatusSelector.find("span.k-input")}
    }

    def clickOnUpdateButton() {
        waitFor { updateButton.click() }
    }

    def clickOnCloseButton() {
        waitFor { closeButton.click() }
    }

    def clickOnTagsInput() {
        tagsInput.click()
    }

    def clickOnTagsDropDown() {
        waitFor { tagsDropDown.present }
        js.'$("tds-asset-tag-selector .component-action-open").click()'
        waitFor{tagNamesSelector.size() > 1}
    }

    def selectTagByName(tagName) {
        clickOnTagsDropDown()
        def tag = tagNamesSelector.find { it.text().trim() == tagName }
        commonsModule.goToElement tag // to avoid stale element error, auto scroll and go to it
        tag.click()
    }

    def selectRandomTag(int numberOfTags) {
        clickOnTagsDropDown()
        def randomTags = CommonActions.getRandomOptions tagNamesSelector, numberOfTags
        def randomTagNames = []
        randomTags.each { // get tags text before close DD and lost references
            commonsModule.goToElement it
            randomTagNames.add it.text().trim()
        }
        clickOnTagsInput() // close DD for next action

        randomTagNames.each {
            selectTagByName it
        }
        randomTagNames
    }

    def verifyDisplayedTagsByName(tagNamesList) {
        def found = false
        tagNamesList.each { tagName ->
            if (tagNamesSelector.find { it.text().trim() == tagName }) {
                found = true
            }
            assert found, "$tagName was not found in the tags list"
        }
        clickOnTagsInput() // close DD for next action
        true // assertion is inside iteration, just prevent this break
    }

    def removeAllTagsIfExists() {
        try {
            // try to delete a list of tags
            waitFor(1) { deleteTagIcon.size() > 1 }
            deleteTagIcon.each {
                it.click()
            }
        } catch (WaitTimeoutException e) {
            // just one tag found delete it
            try {
                waitFor(1) { deleteTagIcon.displayed }
                deleteTagIcon.click()
            } catch (WaitTimeoutException e2) {
                // no tags selected
            }
        }
    }

    def verifyNoTagsSelected() {
        !commonsModule.verifyElementDisplayed(assetTagSelector.find("kendo-taglist li"))
    }

    def deleteAsset(){
        commonsModule.goToElement(deleteButton)
        waitFor{deleteButton.click()}
        commonsModule.clickOnButtonPromptModalByText("OK")
    }
}