package specs.Company

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Admin.CompanyDetailsPage
import pages.Admin.ListCompaniesPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import modules.CommonsModule

@Stepwise
class CompanyDeletionSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static nowDate = new Date().format("MM/dd/yyyy")
    static companyName = baseName +" "+ randStr
    static companyInfo = [
        name: companyName,
        comment: "Comment for company "+ companyName +" created by QA E2E Scripts",
        isPartner: false,
        dateCreated: nowDate,
        lastUpdated: nowDate
    ]
    static successCreationMessage = "PartyGroup "+companyInfo.name+" created"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        adminModule.goToListCompanies()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Confirmation message is displayed"() {
        given: 'The user has selected a Company'
            at ListCompaniesPage
            filterByName "QAE2E"
            clickOnFirstElement()
            at CompanyDetailsPage
        when: 'The user clicks on delete'
            clickDelete()
        then: 'A confirmation message is displayed'
        //withConfirm(false){waitFor {tdModalDeleteBtn.click() }}
    }

    def "2. User Cancels Deletion"() {
        when: 'The user cancels the deletion'

        then: 'Company List Page should be displayed'
            at ListCompaniesPage
    }

    def "4. Certify company information"() {
        when: 'The user filters by name'
            filterByName companyInfo.name
        then: 'Company info displayed in grid is correct'
            getCompanyNameText() == companyInfo.name
            hasCompanyPartner() == " " // is not partner then blank displayed
            getDateCreatedText().contains companyInfo.dateCreated
            getLastUpdatedText().contains companyInfo.lastUpdated
    }
}