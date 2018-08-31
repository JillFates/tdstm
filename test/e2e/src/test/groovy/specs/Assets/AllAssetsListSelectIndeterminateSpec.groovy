package specs.Assets

import pages.AssetViewManager.ViewPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise


@Stepwise
class AllAssetsListSelectIndeterminateSpec extends GebReportingSpec {

    def testKey
    static testCount
    static assetClass = "Application"
    static description = "QAE2E"
    static environment = "Production"

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

    def "1. Certify indeterminate state is checked"(){
        given: 'The user is on the All Assets page'
            at ViewPage
        when: 'The user clicks in select all assets'
            clickOnSelectAllAssets() // click to select all state
        then: 'Bulk change button is displayed'
            waitForBulkChangeButtonDisplayed() // wait and validation
        when: 'The user clicks in select all assets again'
            clickOnSelectAllAssets() // click to select all in indeterminate state
        then: 'Bulk change button still displayed'
            waitForBulkChangeButtonDisplayed() // wait and validation
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "2. Certify first and last page elements are all checked"(){
        when: 'The user goes to Last page'
             commonsModule.goToLastKendoGridPage()
        then: 'All checkboxes are checked in page'
            checkedItems() == true
        when: 'The user goes to First page'
            commonsModule.goToFirstKendoGridPage()
        then: 'All checkboxes are checked in page'
            checkedItems() == true
    }

    def "2. Certify indeterminate state is checked filtering by asset class"(){
        when: 'The user filters by asset class Application'
            filterByAssetClass assetClass
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
        and: 'All checkboxes are checked in page'
            checkedItems() == true
    }

    def "3. Certify indeterminate state is checked filtering by description"(){
        when: 'The user filters by asset class Application'
            filterByDescription description
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "4. Certify indeterminate state is checked filtering by environment"(){
        when: 'The user filters by asset class Application'
            filterByEnvironment environment
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "5. Certify indeterminate state is checked clearing all applied filters"(){
        when: 'The user clears all filters'
            clearAllAppliedFilters()
        then: 'Applied filters are clean'
            getFilterAssetClassText() == ""
            getFilterAssetClassText() == ""
            getFilterAssetClassText() == ""
        and: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "6. Certify indeterminate state is checked filtering Just planning"(){
        when: 'The user clicks in Just Planning checkbox'
            clickOnJustPlanningCheckbox()
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "7. Certify unchecked assets"(){
        when: 'The user clicks in select all assets'
            clickOnSelectAllAssets() // click to select all state
        then: 'Bulk change button is disabled'
            waitForBulkChangeButtonDisabled() // wait and validation
        and: 'All checkboxes are unchecked in page'
            checkedItems(false) == true
    }
}