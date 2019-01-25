package specs.Admin.Company

import geb.spock.GebReportingSpec
import pages.Admin.Company.CompanyDetailsPage
import pages.Admin.Company.CompanyEditionPage
import pages.Admin.Company.CompanyCreationPage
import pages.Admin.Company.ListCompaniesPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import utils.CommonActions

/**
 * @author ingrid
 */

@Stepwise
class CompanyEditionSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static nowDate = new Date().format("MM/dd/yyyy")
    static baseName = "QAE2E Edit Co spec"
    static companyName = baseName +" "+ randStr
    static companyComment="Comment for company "+ companyName +" created by QA E2E Scripts"
    static initPartnerValue=false;
    static companyInfo = [
            name: companyName,
            comment: companyComment ,
            isPartner: false,
            dateCreated: nowDate,
            lastUpdated: nowDate
    ]
    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        adminModule.goToListCompanies()
        //a company is created so it can be edited later (TM-13962)
        at ListCompaniesPage
        clickOnCreateButton()
        at CompanyCreationPage
        createCompany companyInfo
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

    def "1. User Edits a Company"() {
        given: "The user has selected a Company"
            at ListCompaniesPage
            filterByName baseName
            clickOnFirstElement()
            at CompanyDetailsPage
            initPartnerValue=hasCompanyPartner()
            companyName=getCompanyNameText()
            companyComment=getCompanyCommentText()
        when:"The user edits the company"
            clickEdit()
            at CompanyEditionPage
            editCompany()
        then: "The user is led to Company Details page"
            at CompanyDetailsPage
        and: "A message stating the company was updated is displayed"
            validateMessage "PartyGroup "+companyName+" Edited updated"
        and:'The changes in Company Name are saved'
            validateCompanyName companyName + " Edited"
        and:'The changes in Comments are saved'
            validateComment companyComment + " Edited"
        and: "Partner Value has changed"
            validatePartnerValue(!initPartnerValue)
    }

    def "2. Changes are reflected in Companies' list"(){
        given: "User is in List Companies Page"
            at MenuPage
            adminModule.goToListCompanies()
            at ListCompaniesPage
        when: "The user filters by the edited company"
            filterByName companyName + " Edited"
        then: "The company is listed with the 'Edited' word added"
            validateCompanyIsListed(companyName + " Edited")
        and: "The partner field displays the opposite value it initially had"
            validatePartnerField(initPartnerValue)
    }
}
