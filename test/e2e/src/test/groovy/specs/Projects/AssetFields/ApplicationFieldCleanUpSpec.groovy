package specs.Projects.AssetFields

import pages.Projects.AssetFields.AssetFieldSettingsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import spock.lang.Shared

/**
 * This spec is to search by QAE2E Field Settings and clean up cases in which more than 2 are being displayed.
 * @author Sebastian Bigatton
 */

@Stepwise
class ApplicationFieldCleanUpSpec extends GebReportingSpec{

    def testKey
    static testCount
    static baseName = "QAE2E"
    @Shared
    static minNumberOfFieldsPresent = 2

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

    def "1. The user clean up QAE2E fields if more than 2 are displayed"() {
        given: 'The User is in Assets Fields Settings page'
            at AssetFieldSettingsPage
        when: 'The user filters by label'
            filterByName baseName
        then: 'The user removes fields if needed'
            bulkDelete minNumberOfFieldsPresent
    }

    def "2. The user verifies no more than 2 QAE2E fields are dispayed"() {
        when: 'The user filters by label'
            filterByName baseName
        then: 'No more than min QAE2E fields are displayed'
            verifyRowsCountDisplayedByGivenNumber minNumberOfFieldsPresent
    }
}