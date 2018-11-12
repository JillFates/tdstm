package specs.Planning.Bundle

/**
 * This Spec tests bundle list filters
 * @author ingrid
 */

import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Planning.Bundle.ListBundlesPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class SearchBundlesSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    static nonExName="NoBundleWithThisExists"
    static randStr = CommonActions.getRandomString()
    static qty=new Random().nextInt(20) + 1
    static def today = new Date()

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        planningModule.goToListBundles()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Filter Bundle By Name"() {
        given: 'The User is in Bundle List'
            at ListBundlesPage
        when: 'The user filter Bundles by name'
            filterByName baseName
            selectFilter()
        then: 'The Bundles Listed contain the specified text'
            validateFilteredByName(baseName)
    }

    def "2. Filter Bundle by Description"(){
        given: 'The User is in Bundle List'
            clearNameFilter()
        when: 'The user filter Bundles by name'
            filterByDesc baseName
            selectDescFilter()
        then: 'The Bundles Listed contain the specified text'
            validateFilteredByDesc(baseName)
    }

    def "3. Filter by Planning"(){
        given: 'The user is in Bundle List'
            clearDescription()
        when: 'The user filters by Planning bundles'
            clickPlanningFilter()
        then: 'All the bundles listed (if any )are planning bundles'
            validateFilteredByPlanning(true) || validatePagerInfo("No items to display")
    }

    def "4. Filter by non Planning"(){
        when: 'The user filters by Planning bundles'
            clickNonPlanningFilter()
        then: 'All the bundles listed (if any )are planning bundles'
             validateFilteredByPlanning(false) || validatePagerInfo("No items to display")
    }

    def "5. Filter by asset quantity"(){
        when: 'The user filters by Asset quantity'
            clearPlanningFilter()
            filterByQuantity("0"+qty)
        then: 'Either no results are returned or every listed bundle has the given number of assets'
            validateAssetQtyFilter(qty) || validatePagerInfo("No items to display")
    }

    def "6. Filter by start date"(){
        when: 'The user filters bundles by Start Date'
             clearAssetQtty()
             def today = filterByDate(today,true)
        then: 'All the bundles listed have the expected start date'
             validateStartDate(today)
    }

    def "7. Filter by completion date"(){
        when: 'The user filters bundles by Completion Date'
            clearStartingDate()
            def tomorrow = filterByDate(today+1,false)
        then: 'All the bundles listed have the expected Completion date'
            validateCompletionDate(tomorrow)
        }

    def "8. Filter by non-existent name"(){
        when: 'The user filters by a name no bundle will have'
            clearCompletionDate()
            filterByName(nonExName)
        then: 'There are no rows returned'
            numberOfRows()==0
            validatePagerInfo("No items to display")
       }

    def "9. Filter by non-existent description"(){
        when: 'The user filters by a description no bundle will have'
             clearNameFilter()
             filterByDesc(nonExName)
        then: 'There are no rows returned'
            numberOfRows()==0
            validatePagerInfo("No items to display")
    }

    def "10. Negative start date scenario"(){
        when: 'The user filters by a start date no bundle will have'
             clearDescription()
             filterByDate(today-9000,true)
        then: 'There are no rows returned'
            numberOfRows()==0
            validatePagerInfo("No items to display")
    }

    def "11. Negative completion date scenario"(){
        when: 'The user filters by a start date no bundle will have'
             clearStartingDate()
             filterByDate(today-9000,false)
        then: 'There are no rows returned'
            numberOfRows()==0
            validatePagerInfo("No items to display")
       }
}