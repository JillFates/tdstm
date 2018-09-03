package specs.ETLScripts

import pages.ETLScripts.*
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class ETLScriptsListSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr = CommonActions.getRandomString() + " "
    static E2E = "E2E DS"
    static datascriptName = randStr + E2E + " Name"
    static datascriptDescription = randStr + E2E + " Description"
    static datascriptDateCreated = new Date().format("EEEE, MMMM d, yyyy")// this format is required for kendo date picker
    static datascriptProvider
    static firstDSInformation

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()

        at MenuPage
        projectsModule.goToETLScripts()
        at ETLScriptsPage
        createBtn.click()
        at CreateETLScriptsPage
        waitFor{providerDropdown.click()}
        //We select the latest provider that was created
        waitFor{latestProvider.displayed}
        datascriptProvider = latestProvider.text().trim()
        latestProvider.click()
        waitFor{datascriptDescField.click()}
        datascriptDescField = datascriptDescription
        datascriptNameField = datascriptName
        waitFor {datascriptSaveBtn.isDisplayed()}
        waitFor {datascriptSaveBtn.click()}
        at ETLScriptsDetailsPage
        clickOnXButton()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }


    def "1. The User finds a ETLScripts already created filtering by name"() {
        given: 'The User is in ETLScripts list page'
            at ETLScriptsPage
        when: 'The user fill Filter Name'
            filterByName datascriptName
        then: 'One ETLScript row should be displayed'
            getDSRowsSize() == 1
        when: 'The User cleans Filter Name'
            removeNameFilter()
        then: 'At least one ETLScript row should be displayed'
            getDSRowsSize() >= 1
    }

    def "2. The User finds a ETLScript filtering by date created"() {
        given: 'The User is in ETLScript list page'
            at ETLScriptsPage
        when: 'The user fill Filter Date Create'
            filterByDateCreated datascriptDateCreated
        then: 'At least one ETLScript row should be displayed'
            getDSRowsSize() >= 1
    }

    def "3. The User finds a ETLScripts already created filtering by description"() {
        given: 'The User is in ETLScripts list page'
            at ETLScriptsPage
        when: 'The user fill Filter Description'
            filterByDescription datascriptDescription
        then: 'One ETLScript row should be displayed'
            getDSRowsSize() == 1
        when: 'The User cleans Filter Description'
            removeDescriptionFilter()
        then: 'At least one ETLScript row should be displayed'
            getDSRowsSize() >= 1
    }

    def "4. The User play around refreshing the grid"() {
        given: 'The User is in ETLScript list page'
            at ETLScriptsPage
        when: 'The user refreshes ETLScripts grid'
            clickOnRefreshIcon()
        then: 'At least one ETLScript row should be displayed'
            getDSRowsSize() >= 1
    }

    def "5. The User certifies ETLScripts information is properly displayed"() {
        given: 'The user collects first ETLScript information from grid'
            firstDSInformation = collectFirstDSInfoDisplayedInGrid()
        and: 'The user clicks on ETLScript row'
            clickOnFirstGridRow()
        when: 'ETLScripts details modal is opened'
            at ETLScriptsDetailsPage
        then: 'ETLScripts information is properly displayed'
            getDSProviderLabelText() == firstDSInformation.provider
            getDSNameLabelText() == firstDSInformation.name
            getDSModeLabelText() == firstDSInformation.mode
            getDSDescriptionLabelText() == firstDSInformation.description
    }

    def "6. The user order by Asc/Desc  Name"() {
        given: 'The user close the modal'
            clickOnXButton()
        when: 'The User is in ETLScripts list page'
            at ETLScriptsPage
        then: 'Grid is ordered by asc name'
            isOrderedByName "asc"
        when: 'The User clicks on Name header'
            clickOnNameHeader()
        then: 'Grid is ordered by desc name'
            isOrderedByName "desc"
    }

    def "7. The User finds a ETLScripts filtering by different combinations"() {
        given: 'The User is in ETLScripts list page'
            at ETLScriptsPage
        when: 'The user fill Filter Name + Date Created already selected'
            filterByName datascriptName
        then: 'One ETLScript row should be displayed'
            getDSRowsSize() == 1
        when: 'The User cleans adds Filter Description'
            filterByDescription datascriptDescription
        then: 'One ETLScript row should be displayed'
            getDSRowsSize() == 1
        when: 'The user removes some filters'
            removeDescriptionFilter()
            removeNameFilter()
            removeDateCreateFilter()
        and: 'The User cleans adds Filter Provider'
            filterByProvider datascriptProvider
        then: 'At least one ETLScript row should be displayed'
            getDSRowsSize() >= 1
        when: 'The User cleans adds Filter Mode'
            filterByMode "Import"
        then: 'At least one ETLScript row should be displayed'
            getDSRowsSize() >= 1
        when: 'The user fill Filter Name + Date Created already selected'
            filterByName datascriptName
        then: 'One ETLScript row should be displayed'
            getDSRowsSize() == 1
    }

}