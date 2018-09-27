package specs.Projects.Tags

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Projects.Tags.TagsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class TagsCreationSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static nowDate = new Date().format("MM/dd/yyyy")
    static customTag = [
            "name": "$baseName $randStr Tag",
            "description": "Custom $baseName $randStr Tag description",
            "color": null,
            "assets": "0",
            "dateCreated": nowDate,
            "lastModified": nowDate
    ]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The User navigates to Manage Tags Page"() {
        given: 'The User is logged in'
            at MenuPage
        when: 'The User clicks on Projects > Tags'
            projectsModule.goToTagsPage()
        then: 'Manage Tags Page is displayed'
            at TagsPage
    }

    def "2. The user certifies that 2 Cancel and Save buttons are going to appear"() {
        given: 'The User is Manage Tags Page'
            at TagsPage
        when: 'The User clicks on Create button'
            clickOnCreateTagButton()
        then: 'Cancel button is displayed and enabled'
            isCancelButtonDisplayed() == true
        and: 'Cancel button is displayed and disabled'
            isSaveButtonDisplayed() == true
            isSaveButtonDisabled() == "true"
        when: 'The User clicks on Cancel button'
            clickOnCancelButton()
        then: 'The User verifies row is not editable'
            isFirstTagRowNameNotEditable() == true
    }

    def "3. The User starts creating a custom tag and certifies some are not editable"(){
        given: 'The User is Manage Tags Page'
            at TagsPage
        when: 'The User clicks on Create button'
            clickOnCreateTagButton()
        and: 'The user fills Name, Description and selects Color'
            fillInFields customTag
            customTag.color = getTagColorHexText()
        then: 'The User certifies Assets, Date Created and Last Modified are not editable'
            isAssetsNotEditableOnCreation() == true
            isDateCreatedNotEditableOnCreation() == true
            isLastModifiedNotEditableOnCreation() == true
    }

    def "4. The User creates a custom tag"(){
        given: 'The User filled some fields'
            isSaveButtonDisabled() == false
        when: 'The User clicks on Save button'
            clickOnSaveButton()
        then: 'The User verifies row is not editable'
            isFirstTagRowNameNotEditable() == true
    }

    def "5. The User verifies tags info displayed"() {
        given: 'The User is in Manage Tags page'
            at TagsPage
        when: 'The User filters by name'
            filterByName customTag.name
        then: 'The User verifies row info displayed is correct'
            getTagNameText() == customTag.name
            getTagDescriptionText() == customTag.description
            getTagColorHexText() == customTag.color
            getTagAssetsText() == customTag.assets
            getTagDateCreatedText() == customTag.dateCreated
            getTagLastModifiedText() == customTag.lastModified
    }
}