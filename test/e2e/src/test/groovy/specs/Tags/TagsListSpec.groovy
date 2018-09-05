package specs.Tags

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Tags.TagsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class TagsListSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static noTagsMessage = "No records available."
    static tagName
    static tagDescription

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        projectsModule.goToTagsPage()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Search tags but no records found"() {
        given: 'The User is in the Tags Section'
            at TagsPage
        when: 'The User filters by name'
            filterByName baseName + "NoResultsWillFound"
        then: 'No records message should be displayed'
            noTagsDisplayedInGrid noTagsMessage
    }

    def "2. Find tag filtering by different fields"() {
        when: 'The User filters by name'
            filterByName baseName
        and: 'The user gets full first tag name and description'
            tagName = getTagNameText()
            tagDescription = getTagDescriptionText()
        and: 'The User filters by name'
            filterByName tagName
        then: 'One tag should be displayed and tag info should be populated'
            verifyTagInformation tagName, tagDescription
        when: 'The user removes filter by name'
            removeNameFilter()
        and: 'The User filters by description'
            filterByDescription tagDescription
        then: 'One tag should be displayed and tag info should be populated'
            verifyTagInformation tagName, tagDescription
        when: 'The user filters by name and description'
            filterByName tagName
            filterByDescription tagDescription
        then: 'One tag should be displayed and tag info should be populated'
            verifyTagInformation tagName, tagDescription
    }
}