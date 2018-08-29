package specs.Company

/**
 * @author Sebastian Bigatton
 */

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Admin.CompanyCreationPage
import pages.Admin.CompanyDetailsPage
import pages.Admin.ListCompaniesPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class CompanyCreationWithPartnersSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static nowDate = new Date().format("MM/dd/yyyy")
    static companyName = baseName +" "+ randStr
    static companyInfo = [
        name: companyName,
        comment: "Comment for company "+ companyName +" created by QA E2E Scripts",
        isPartner: true,
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

    def "1. Open Create Company Page"() {
        given: 'The User is on the Company List Page'
            at ListCompaniesPage
        when: 'The User clicks on Create Company button'
            clickOnCreateButton()
        then: 'Project Creation Page is displayed'
            at CompanyCreationPage
    }

    def "2. Create Company"() {
        when: 'The user fills and saves info'
            createCompany companyInfo
        then: 'Company Details Page is displayed'
            at CompanyDetailsPage
        and: 'Message saying project created is displayed'
            getTextMessage().contains successCreationMessage
        and: 'Company info displayed is correct'
            getCompanyNameText() == companyInfo.name
            getCompanyCommentText() == companyInfo.comment
            hasCompanyPartner() == companyInfo.isPartner
            getDateCreatedText().contains companyInfo.dateCreated
            getLastUpdatedText().contains companyInfo.lastUpdated
    }

    def "3. Go to Company List Page"() {
        given: 'The user navigates to Admin menu'
            at MenuPage
        when: 'The user clicks List Companies'
            adminModule.goToListCompanies()
        then: 'Company List Page should be displayed'
            at ListCompaniesPage
    }

    def "4. Certify company information"() {
        when: 'The user filters by name'
            filterByName companyInfo.name
        then: 'Company info displayed in grid is correct'
            getCompanyNameText() == companyInfo.name
            hasCompanyPartner() == "Yes"
            getDateCreatedText().contains companyInfo.dateCreated
            getLastUpdatedText().contains companyInfo.lastUpdated
    }
}