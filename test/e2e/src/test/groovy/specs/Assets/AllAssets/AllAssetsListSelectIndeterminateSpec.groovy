package specs.Assets.AllAssets

import pages.Assets.AssetViews.ViewPage
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
    static assetsCountToBeSelected = 3

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

    def "1. Certify indeterminate state is checked"(){
        given: 'The user is on the All Assets page'
            at ViewPage
        and: 'The user removes Just Planning if its checked'
            unCheckJustPlanning()
        when: 'The user clicks in select all assets to get indeterminate state'
            checkIndetermitateItems()
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

    def "3. Certify indeterminate state is checked filtering by asset class"(){
        when: 'The user filters by asset class Application'
            filterByAssetClass assetClass
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
        and: 'All checkboxes are checked in page'
            checkedItems() == true
    }

    def "4. Certify indeterminate state is checked filtering by description"(){
        when: 'The user filters by asset class Application'
            filterByDescription description
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "5. Certify indeterminate state is checked filtering by environment"(){
        when: 'The user filters by asset class Application'
            filterByEnvironment environment
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "6. Certify indeterminate state is checked clearing all applied filters"(){
        when: 'The user clears all filters'
            clearAllAppliedFilters()
        then: 'Applied filters are clean'
            getFilterAssetClassText() == ""
            getFilterAssetClassText() == ""
            getFilterAssetClassText() == ""
        and: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "7. Certify indeterminate state is checked filtering Just planning"(){
        when: 'The user clicks in Just Planning checkbox'
            checkJustPlanning()
        then: 'We verify indeterminate state is checked'
            getSelectIndeterminateState() == true
    }

    def "8. Certify unchecked assets"(){
        when: 'The user clicks in select all assets to get unchecked state'
            unCheckAllItems()
        then: 'All checkboxes are unchecked in page'
            checkedItems(false) == true
    }

    def "9. Certify selected assets count for all assets state"(){
        when: "The user clicks on select all assets to get all checked state"
            checkAllItems()
            def paginationValue = getPaginationSelectValue()
        then: 'All checkboxes are checked in page'
            checkedItems() == true
        and: "Selected assets value is correct"
            verifySelectedAssetsText paginationValue
        when: "The user moves to next page"
            commonsModule.goToTargetKendoGridPage "next"
        then: 'All checkboxes are unchecked in page'
            checkedItems(false) == true
        and: "Selected assets text is not displayed"
            !verifySelectedAssetsTextDisplayed()
    }

    def "10. Certify selected assets count for indeterminate state"(){
        given: "The user moves to first page"
            commonsModule.goToFirstKendoGridPage()
        when: "The user clicks on select all assets to get indeterminate state"
            checkIndetermitateItems()
            def totalAssetsValue = getTotalNumberOfAssetsFromBottomPager()
        then: 'All checkboxes are checked in page'
            checkedItems() == true
        and: "Selected assets value is correct"
            verifySelectedAssetsText totalAssetsValue
        when: "The user moves to next page"
            commonsModule.goToTargetKendoGridPage "next"
        then: 'All checkboxes are checked in page'
            checkedItems() == true
        and: "Selected assets value is correct"
            verifySelectedAssetsText totalAssetsValue
    }

    def "11. Certify random selected assets count"(){
        given: "The user moves to first page"
            commonsModule.goToFirstKendoGridPage()
        and: "The user clicks on select all assets to get unchecked state"
            unCheckAllItems()
        and: "Selected assets text is not displayed"
            !verifySelectedAssetsTextDisplayed()
        when: "The user selects random assets"
            selectRandomAssetsAndGetNames assetsCountToBeSelected
        then: 'Bulk change button is displayed'
            waitForBulkChangeButtonDisplayed() // wait and validation
        and: "Selected assets value is correct"
            verifySelectedAssetsText assetsCountToBeSelected
    }
}