package specs.Datascripts

import pages.Datascripts.CreateDatascriptPage
import pages.Datascripts.DatascriptDetailsPage
import pages.Datascripts.DatascriptsPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import utils.CommonActions
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class DatascriptListSpec extends GebReportingSpec{

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
        projectsModule.goToDatascripts()
        at DatascriptsPage
        createBtn.click()
        at CreateDatascriptPage
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
        at DatascriptDetailsPage
        clickOnXButton()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }


    def "1. The User finds a datascript already created filtering by name"() {
        given: 'The User is in datascript list page'
            at DatascriptsPage
        when: 'The user fill Filter Name'
            filterByName datascriptName
        then: 'One datascript row should be displayed'
            getDSRowsSize() == 1
        when: 'The User cleans Filter Name'
            removeNameFilter()
        then: 'At least one datascript row should be displayed'
            getDSRowsSize() >= 1
    }

    def "2. The User finds a datascripts filtering by date created"() {
        given: 'The User is in datascript list page'
            at DatascriptsPage
        when: 'The user fill Filter Date Create'
            filterByDateCreated datascriptDateCreated
        then: 'At least one datascript row should be displayed'
            getDSRowsSize() >= 1
    }

    def "3. The User finds a datascript already created filtering by description"() {
        given: 'The User is in datascript list page'
            at DatascriptsPage
        when: 'The user fill Filter Description'
            filterByDescription datascriptDescription
        then: 'One datascript row should be displayed'
            getDSRowsSize() == 1
        when: 'The User cleans Filter Description'
            removeDescriptionFilter()
        then: 'At least one datascript row should be displayed'
            getDSRowsSize() >= 1
    }

    def "4. The User play around refreshing the grid"() {
        given: 'The User is in datascript list page'
            at DatascriptsPage
        when: 'The user refreshes datascript grid'
            clickOnRefreshIcon()
        then: 'At least one datascript row should be displayed'
            getDSRowsSize() >= 1
    }

    def "5. The User certifies datascript information is properly displayed"() {
        given: 'The user collects first Datascript information from grid'
            firstDSInformation = collectFirstDSInfoDisplayedInGrid()
        and: 'The user clicks on datascript row'
            clickOnFirstGridRow()
        when: 'Datascript details modal is opened'
            at DatascriptDetailsPage
        then: 'Datascript information is properly displayed'
            getDSProviderLabelText() == firstDSInformation.provider
            getDSNameLabelText() == firstDSInformation.name
            getDSModeLabelText() == firstDSInformation.mode
            getDSDescriptionLabelText() == firstDSInformation.description
    }

    def "6. The user order by Asc/Desc  Name"() {
        given: 'The user close the modal'
            clickOnXButton()
        when: 'The User is in datascript list page'
            at DatascriptsPage
        then: 'Grid is ordered by asc name'
            isOrderedByName "asc"
        when: 'The User clicks on Name header'
            clickOnNameHeader()
        then: 'Grid is ordered by desc name'
            isOrderedByName "desc"
    }

    def "7. The User finds a datascript filtering by different combinations"() {
        given: 'The User is in datascript list page'
            at DatascriptsPage
        when: 'The user fill Filter Name + Date Created already selected'
            filterByName datascriptName
        then: 'One datascript row should be displayed'
            getDSRowsSize() == 1
        when: 'The User cleans adds Filter Description'
            filterByDescription datascriptDescription
        then: 'One datascript row should be displayed'
            getDSRowsSize() == 1
        when: 'The user removes some filters'
            removeDescriptionFilter()
            removeNameFilter()
            removeDateCreateFilter()
        and: 'The User cleans adds Filter Provider'
            filterByProvider datascriptProvider
        then: 'At least one datascript row should be displayed'
            getDSRowsSize() >= 1
        when: 'The User cleans adds Filter Mode'
            filterByMode "Import"
        then: 'At least one datascript row should be displayed'
            getDSRowsSize() >= 1
        when: 'The user fill Filter Name + Date Created already selected'
            filterByName datascriptName
        then: 'One datascript row should be displayed'
            getDSRowsSize() == 1
    }

}