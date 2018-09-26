package specs.Projects.AssetFields

import pages.Projects.AssetFields.AssetFieldSettingsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import spock.lang.Shared

/**
 * This spec verify some required fields and validations, then creates a custom Application field
 * which wont be displayed and finally verifies it was properly saved.
 * @author Sebastian Bigatton
 */

@Stepwise
class ApplicationNonDisplayedFieldCreationSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    @Shared
    static fieldInfo = [
            "fieldName": null,
            "label": baseName + " Non-Displayed",
            "highlighting": null,
            "required": "No",
            "display": "No",
            "defaultValue": baseName + " Non-Displayed",
            "control": "String",
            "stringMin": "0",
            "stringMax": "200",
            "tooltip": "QAE2E Non-Displayed Application Field for testing purposes"
    ]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        projectsModule.goToAssetFieldSettings()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user verifies default tab displayed"() {
        when: 'The User is in Assets Fields Settings page'
            at AssetFieldSettingsPage
        then: 'Application tab is active and displayed'
            verifyActiveTabByName "Application"
    }

    def "2. The user verifies action button visibility"() {
        when: 'The user clicks on Edit button'
            clickOnEditButton()
        then: 'Save All button is disabled'
            verifySaveAllButtonState true // verifies disabled property
        and: 'Cancel button is enabled'
            verifyCancelButtonState false // verifies disabled property
        when: 'The user clicks on Cancel Button'
            clickOnCancelButton()
        then: 'Asset Field Settings page is displayed'
            at AssetFieldSettingsPage
    }

    def "3. The user verifies required fields and validations"() {
        given: 'The user clicks on Edit button'
            clickOnEditButton()
        when: 'The user clicks on Add Custom Field'
            clickOnAddCustomFieldButton()
        then: 'The required fields and validations related are in place'
            verifyCustomRowStatus()
        when: 'The user clicks on Cancel button'
            clickOnCancelButton()
        then: 'Confirmation modal is displayed'
            commonsModule.verifyConfirmationPrompDialogMessage "You have changes that have not been saved"
        when: 'The user clicks on Cancel button'
            commonsModule.clickOnButtonPromptModalByText "Cancel"
        then: 'Prompt modal is hidden'
            commonsModule.waitForPromptModalHidden()
    }

    def "4. The user creates a Non-Displayed Custom Application field"(){
        when: 'The user fill field information'
            fieldInfo.fieldName = getLabelInputId() // store field name to verify after create
            setLabelName fieldInfo.label
            fieldInfo.highlighting = selectRandomHighlighting() // selects and returns selected to verify after create
            clickOnDisplayCheckbox() // to disable it
            setDefaultValue fieldInfo.defaultValue
            getSelectedControlValueInEdit() == fieldInfo.control
            clickOnControlWheelIcon()
            setMinMaxStringControlLengh fieldInfo.stringMin, fieldInfo.stringMax
            setToolTipHelp fieldInfo.tooltip
        and: 'The user clicks on Save All button'
            clickOnSaveAllButton()
        then: 'Asset Field Settings page is displayed in original state'
            at AssetFieldSettingsPage
    }

    def "5. The user verifies created Application field"() {
        when: 'The user filters by label'
            filterByName fieldInfo.label
        then: 'The user certifies displayed information is correct'
            verifyCustomFieldInfo fieldInfo
    }
}
