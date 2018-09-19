package specs.Projects.Tags

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Projects.Tags.TagsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class TagsEditionSpec extends GebReportingSpec {
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
        at MenuPage
        projectsModule.goToTagsPage()
        at TagsPage
        clickOnCreateTagButton()
        fillInFields customTag
        clickOnSaveButton()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user cancels edition process"() {
        given: 'The User is at Manage Tags Page'
            at TagsPage
        when: 'The User filters by name'
            filterByName customTag.name
        and: 'The User clicks on Edit button'
            clickOnEditButton()
        then: 'Cancel button is displayed and enabled'
            isCancelButtonDisplayed() == true
        when: 'The User clicks on Cancel button'
            clickOnCancelButton()
        then: 'The User verifies row is not editable'
            isFirstTagRowNameNotEditable() == true
    }

    def "2. The User edits a custom tag"(){
        when: 'The User clicks on Edit button'
            clickOnEditButton()
        and: 'The user fills Name, Description and selects Color'
            customTag.name = customTag.name + " Edited"
            customTag.description = customTag.description + " Edited"
            fillInFields customTag
            customTag.color = getTagColorHexText()
        and: 'The User clicks on Save button'
            clickOnSaveButton()
        then: 'The User verifies row is not editable'
            isFirstTagRowNameNotEditable() == true
    }

    def "3 The User verifies tags info displayed"() {
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