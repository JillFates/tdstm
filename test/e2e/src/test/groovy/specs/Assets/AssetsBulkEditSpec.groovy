package specs.Assets

/**
 * @author Sebastian Bigatton
 */

import pages.Assets.BulkChangeEditAssetsPage
import pages.Assets.BulkChangeActionPage
import pages.AssetViewManager.ViewPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import spock.lang.Shared

@Stepwise
class AssetsBulkEditSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    @Shared
    static assetNames = []
    @Shared
    static assetCheckboxNameMap = []
    @Shared
    static selectedTags = []
    static numberOfAssetsToBeEdited = 3
    static noRecordsMessage = "No records available"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        assetsModule.goToAllAssets()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Certify Bulk Change Action modal"(){
        given: 'The user is on the All Assets page'
            at ViewPage
        when: 'The user filters by name QAE2E'
            filterByName baseName
        then: 'The user verifies if no records displayed'
            verifyIfNoRecordsDisplayed noRecordsMessage
        when: 'The user selects random QAE2E or any assets displayed and collects names'
            assetNames = selectRandomAssetsAndGetNames numberOfAssetsToBeEdited
        and: 'The user clicks on Bulk Change button'
            clickOnBulkChangeButton()
        then: 'Bulk Change Action modal is displayed'
            at BulkChangeActionPage
        and: 'Edit radio button is selected'
            getEditRadioButtonStatus() == true
        and: 'Bulk change action message is correct'
            verifyActionMessageText "This action will effect ${assetNames.size()} Asset(s)"
    }

    def "2. Certify assets still selected after cancel process"(){
        when: 'The user clicks on Cancel button'
            clickOnCancelButton()
        and: 'All Assets view is displayed'
            at ViewPage
            assetCheckboxNameMap = getAllCheckedInputNameMap assetNames
        then: 'Selected assets still checked'
            verifyCheckedInputAssetsByName assetNames
    }

    def "3. Certify Bulk Change Edit Assets modal step"(){
        given: 'The user clicks on Bulk Change button'
            clickOnBulkChangeButton()
        and: 'Bulk Change Action modal is displayed'
            at BulkChangeActionPage
        when: 'The User clicks on Next button'
            clickOnNextButton()
        then: 'Bulk Change Edit Assets modal is displayed'
            at BulkChangeEditAssetsPage
    }

    def "4. Certify confirmation update process"(){
        when: 'The User selects field name'
            selectFieldNameByText "Tags"
        and: 'The User selects action'
            selectActionByText "Add to existing"
        and: 'The User selects random value by given text or any'
            commonsModule.selectRandomKendoMultiselectTagOptionByText baseName
            selectedTags = commonsModule.getSelectedTagsFromKendoMultiselect()
        and: 'The User clicks on Next button'
            clickOnNextButton()
        then: 'Delete confirmation modal message is correct'
            commonsModule.verifyConfirmationPrompDialogMessage "You are about to update ${assetNames.size()} Asset(s)"
    }

    def "5. Certify bulk change edit asset process is complete"() {
        when: 'The User clicks on Confirm button'
            commonsModule.clickOnButtonPromptModalByText("Confirm")
        then: 'Confirmation modal is closed'
            commonsModule.waitForPromptModalHidden()
        and: 'All Assets view is displayed'
            at ViewPage
        and: 'All checkboxes are unchecked in page'
            checkedItems false
    }

    def "6. Certify applied changes to assets"(){
        when: 'The user adds custom column'
            addColumnByName "Tags"
        then: 'Tags were added successfully to assets'
                verifyDisplayedTagsByAsset assetCheckboxNameMap, selectedTags
    }
}