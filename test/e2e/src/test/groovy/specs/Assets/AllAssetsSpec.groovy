package specs.Assets

import jdk.nashorn.internal.ir.annotations.Ignore
import pages.Assets.AllAssetsPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import jodd.util.RandomString
import geb.spock.GebReportingSpec
import modules.CommonsModule
import spock.lang.Stepwise
import spock.lang.Ignore


@Stepwise
class AllAssetsSpec extends GebReportingSpec {

    def testKey
    static testCount
    static int dropdownItems = 250 //Helps to set the amount of items that will be displayed in the table
    static originalFirstElementName = ""


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
        when: 'The user goes to the Providers page'
            menuModule.goToAllAssets()

        then: 'The All Assets Page loads with no problem'
            at AllAssetsPage


    }

    def "2. The user sets the pagination, clicks the Just Planning checkbox and no TBD bundles are shown "(){
        given: 'The user is on the All Assets page'
            at AllAssetsPage
        when: 'The user clicks the Just Planning checkbox'
            //Before, we gotta check if it wasn't previously checked.
            if(justPlanningCheck.value()==false)
                waitFor{justPlanningCheck.click()}
        and: 'We wait for the table content to be displayed'
            waitFor{leftTableElements.displayed}
        and: 'We change the pagination to the value set above'
            itemsPerPage.value(dropdownItems)
        and: 'We inspect how many times we have to search on the table for TBD bundles'
            //We get how many items the table has
            def splitItemsString = itemNumberDesc.text().split(" ")
            def numberItems = splitItemsString[splitItemsString.size()-2]
            float numberItemsFloat = Math.ceil(numberItems as Float)
            //We calculate how many times we have to analyze the table
            def amountOfIterations = Math.ceil(numberItemsFloat/dropdownItems)

        then: 'We verify that no Assets have the TBD bundle'
            searchTBD(amountOfIterations) == false
        and: 'We uncheck the Just Planning checkbox'
            waitFor{justPlanningCheck.click()}

    }

    def "3. The user checks and unchecks all the items of the table"(){
        given: 'The user is on the All Assets page'
            at AllAssetsPage
        and: 'The user checks all the items'
            //Before, we gotta check if it wasn't previously checked.
            if(selectAllChecks.value()==false)
                waitFor{selectAllChecks.click()}
        and: 'We verify all the items are checked'
            checkedItems()== true
        when: 'We uncheck all the items'
            waitFor{selectAllChecks.click()}

        then: 'All the items are unchecked again'
            checkedItems()== false

    }

    def "4. The user filters data on different columns"(){
        given: 'The user is on the All Assets page'
            at AllAssetsPage
        when: 'The user filters by the name of the first element'
            waitFor{nameFilter.click()}
            nameFilter=firstElementName.text()
        and: 'The user also filters by the Asset Class of the first element'
            waitFor{assetClassFilter.click()}
            assetClassFilter=firstElementAssetClass.text()

        then: 'We verify that the first elements name matches'
            firstElementName.text()==firstElementName.text()
        and:'Also the Asset Class matches'
            firstElementAssetClass.text()==firstElementAssetClass.text()

    }

    def "5. The user clears the filters"(){
        given: 'The user is on the All Assets page'
            at AllAssetsPage
        when: 'The user clears the Name filter by clicking its X icon'
            waitFor{nameFilterXicon.click()}
        and: 'We verify the name filter text is empty'
            nameFilter.text()== ""
        and: 'We click the Clear filters button'
            waitFor{clearFiltersBtn.click()}

        then: 'We verify that the other filter that was used is now emptied'
            assetClassFilter.text()== ""

    }

    def "6. The user sorts different columns"(){
        given: 'The user is on the All Assets page'
            at AllAssetsPage
        when: 'The user sorts the Description and Name Columns'
            //We store the original value of the first element for later
            originalFirstElementName=firstElementName.text()
            waitFor{descColumn.click()}
            waitFor{nameColumn.click()}

        then: 'The table should be displayed as it originally was shown'
        /*
            When the table is first loaded, it's sorted by name.
            That means that if you sort it by description(like above) and later
            by name again(as above); the table should look like it was originally loaded.
            For that reason, the same element that is now displayed should be the same as before(originalFirstElementName)
         */
            originalFirstElementName==firstElementName.text()

    }

    def "7. The user changes the pagination value again"(){
        given: 'The user is on the All Assets page'
            at AllAssetsPage
        when: 'The user changes the pagination value again'
            itemsPerPage.value(25)

        then: 'The table loads and the elements are present'
            leftTableElements.displayed

    }

}