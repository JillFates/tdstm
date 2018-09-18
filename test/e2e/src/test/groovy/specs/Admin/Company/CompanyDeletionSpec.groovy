package specs.Admin.Company

import geb.spock.GebReportingSpec
import pages.Admin.Company.CompanyDetailsPage
import pages.Admin.Company.ListCompaniesPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

/**
 * @author ingrid
 */

@Stepwise
class CompanyDeletionSpec extends GebReportingSpec {
    def testKey
    static testCount
    static baseName = "QAE2E"
    static companyName =""
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

    def "1. User Cancels Company Deletion"() {
        given: 'The user has selected a Company'
            at ListCompaniesPage
            filterByName baseName
            clickOnFirstElement()
            at CompanyDetailsPage
            companyName=getCompanyNameText()
        when: 'The user clicks on delete and cancels'
            deleteCompany(false)
        then: 'The company is not deleted'
            validateCompanyName companyName
        and:'User is still at company details page'
            at CompanyDetailsPage
    }

    def "2. User Deletes the Company"() {
        when: 'The user deletes the company'
            deleteCompany(true)
        then: 'The user is led to Company List page'
            at ListCompaniesPage
        and: 'A message stating the company was deleted is displayed'
            validateMessage("PartyGroup "+companyName+" deleted")
    }

    def "3 The Company is no longer listed"(){
        when: 'The user filters by the company name'
            filterByName companyName
        then:  'The company is no longer listed'
            validateNoResultsAreReturned()
    }
}