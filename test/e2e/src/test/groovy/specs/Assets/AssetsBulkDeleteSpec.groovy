package specs.Assets

/**
 * @author Sebastian Bigatton
 */

import pages.Assets.BulkChangeActionPage
import pages.AssetViewManager.ViewPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import spock.lang.Shared

@Stepwise
class AssetsBulkDeleteSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    @Shared
    static assetNames = []
    @Shared
    static assetCheckboxNameMap = []
    static numberOfAssetsToBeDeleted = 2

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        menuModule.goToAllAssets()
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
        and: 'The user selects random assets and collects names'
            assetNames = selectRandomAssetsAndGetNames numberOfAssetsToBeDeleted
        and: 'The user clicks on Bulk Change button'
            clickOnBulkChangeButton()
        then: 'Bulk Change Action modal is displayed'
            at BulkChangeActionPage
        and: 'Edit radio button is selected'
            getEditRadioButtonStatus() == true
        and: 'Bulk change action message is correct'
            getActionMessageText() == "This action will effect ${assetNames.size()} Asset(s)"
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

    def "3. Certify delete confirmation modal message"(){
        given: 'The user clicks on Bulk Change button'
            clickOnBulkChangeButton()
        and: 'Bulk Change Action modal is displayed'
            at BulkChangeActionPage
        when: 'The User clicks on Delete radio button'
            clickOnDeleteRadioButton()
        and: 'The User clicks on Next button'
            clickOnNextButton()
        then: 'Delete confirmation modal message is correct'
            commonsModule.getDeleteAlertMessageText().contains "You are about to delete ${assetNames.size()} Asset(s)"
    }

    def "4. Certify assets still selected after cancel delete confirmation process"(){
        when: 'The User clicks on Cancel button'
            commonsModule.clickOnButtonPromptModalByText("Cancel")
        then: 'Confirmation modal is closed'
            commonsModule.waitForPromptModalHidden()
        and: 'Bulk Change Action is closed'
            commonsModule.waitForDialogModalHidden()
        and: 'All Assets view is displayed'
            at ViewPage
        and: 'Selected assets still checked'
            verifyCheckedInputAssetsByName assetNames
    }

    def "5. Certify deleted assets"(){
        given: 'The user clicks on Bulk Change button'
            clickOnBulkChangeButton()
        and: 'Bulk Change Action modal is displayed'
            at BulkChangeActionPage
        when: 'The User clicks on Delete radio button'
            clickOnDeleteRadioButton()
        and: 'The User clicks on Next button'
            clickOnNextButton()
        and: 'The User clicks on Confirm button'
            commonsModule.clickOnButtonPromptModalByText("Confirm")
        then: 'All Assets view is displayed'
            at ViewPage
        and: 'Assets are not displayed'
            verifyDeletedAssetsByCheckboxName assetCheckboxNameMap
    }
}