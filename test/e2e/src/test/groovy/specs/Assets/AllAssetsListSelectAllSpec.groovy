package specs.Assets

import pages.AssetViewManager.ViewPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class AllAssetsListSelectAllSpec extends GebReportingSpec {

    def testKey
    static testCount
    static int dropdownItems = 250 //Sets the amount of items that will be displayed in the table
    static originalFirstElementName
    static assetsCountToBeSelected = 3

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

    def "1. The user navigates to the All Assets page"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The user goes to the All Assets page'
            assetsModule.goToAllAssets()

        then: 'The All Assets Page loads with no problem'
            at ViewPage

    }

    def "2. The user checks and unchecks all the items of the table"(){
        given: 'The user is on the All Assets page'
            at ViewPage
        and: 'The user removes Just Planning if its checked'
            unCheckJustPlanning()
        and: 'The user checks all the items'
            waitFor {view.displayed}
            checkAllItems()
        and: 'We verify all the items are checked'
            checkedItems()== true
        when: 'We uncheck all the items'
            unCheckAllItems()
        then: 'All the items are unchecked again'
            checkedItems(false)== true

    }

    def "3. The user filters data on different columns"(){
        given: 'The user is on the All Assets page'
            at ViewPage
        when: 'The user filters by the name of the first element'
            originalFirstElementName = getFirstElementNameText()
            def originalFirstElementClass = getFirstElementClassText()
            filterByName originalFirstElementName
        and: 'The user also filters by the Asset Class of the first element'
            filterByAssetClass originalFirstElementClass

        then: 'We verify that the first elements name matches'
            getFirstElementNameText() == originalFirstElementName
        and:'Also the Asset Class matches'
            getFirstElementClassText() == originalFirstElementClass

    }

    def "4. The user clears the filters"(){
        given: 'The user is on the All Assets page'
            at ViewPage
        when: 'The user clears the Name filter by clicking its X icon'
            waitFor{nameFilterXicon.click()}
        and: 'We verify the name filter text is empty'
            nameFilter.text()== ""
        and: 'We click the Clear filters button'
            waitFor{clearBtn.click()}

        then: 'We verify that the other filter that was used is now emptied'
            assetClassFilter.text()== ""

    }

    def "5. The user sorts different columns"(){
        given: 'The user is on the All Assets page'
            at ViewPage
        when: 'The user sorts the Description and Name Columns'
            waitFor {view.displayed}
            //We store the original value of the first element for later
            originalFirstElementName = getFirstElementNameText()
            waitFor{descColumn.click()}
            waitFor{nameColumn.displayed}
            waitFor{refreshBtn.click()}
            waitFor{nameColumn.click()}
            waitFor{refreshBtn.click()}

        then: 'The table should be displayed as it was originally shown'
        /*
            When the table is first loaded, it's sorted by name.
            That means that if you sort it by description(like above) and later
            by name again(as above); the table should look like it was originally loaded.
            For that reason, the same element that is now displayed should be the same as before(originalFirstElementName)
         */
            waitFor{refreshBtn.click()}
            waitFor {view.displayed}
        getFirstElementNameText() == originalFirstElementName

    }

    def "6. The user sets the pagination, clicks the Just Planning checkbox and no TBD bundles are shown "(){
        given: 'The user is on the All Assets page'
            at ViewPage
        when: 'The user clicks the Just Planning checkbox'
            checkJustPlanning()
        and: 'We wait for the table content to be displayed'
            waitFor{leftTableElements.displayed}
            waitFor {view.displayed}
        and: 'We change the pagination to the value set above'
            itemsPerPage.value(dropdownItems)

        then: 'We verify that no Assets have the TBD bundle'
            searchTBD(dropdownItems) == false
        and: 'We uncheck the Just Planning checkbox'
            waitFor{justPlanningCheck.click()}

    }

    def "7. The user changes the pagination value again"(){
        given: 'The user is on the All Assets page'
            at ViewPage
        when: 'The user changes the pagination value again'
            waitFor {view.displayed}
            itemsPerPage.value(25)

        then: 'The table loads and the elements are present'
            leftTableElements.displayed
            waitFor {view.displayed}
    }

    def "8. Certify selected assets count for all assets state"(){
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

    def "9. Certify selected assets count for indeterminate state"(){
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

    def "10. Certify random selected assets count"(){
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