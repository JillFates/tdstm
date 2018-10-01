package specs.Admin.Staff

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Admin.Staff.StaffListPage
import pages.Admin.Staff.StaffCreationPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

/**
 * @author ingrid
 */

@Stepwise
class StaffSearchAndFilteringSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()

    //Define the names for the Staff that you will Create and Edit
    static baseName = "QAE2E"
    static userProject = "TM-Demo"
    static userCompany = "TM Demo"
    static teamName = "Account Manager"
    static firstName = baseName + randStr + "First"
    static middleName = baseName + randStr + "Middle"
    static lastName = baseName + randStr + "Last"
    static userName = baseName + randStr + "Test"
    //static userPass = baseName + randStr + "Pass*"
    static userEmail = baseName + randStr + "testuser@transitionaldata.com"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        /**
         * Staff is created as part of setup for the tests
         */
        adminModule.goToAdminListStaff()
        at StaffListPage
        waitFor {createStaffBtn.click()}
        at StaffCreationPage
        scModalFirstName = firstName
        scModalMiddleName = middleName
        scModalLastName = lastName
        scEmail = userEmail
        scModalAddTeam.click()
        waitFor {scModalTeamSelector.present}
        scModalTeamSelector = teamName
        waitFor { scModalSaveBtn.click() }
        at StaffListPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The user performs search for inexistent user"(){
        given: 'The user is at User List page'
            at StaffListPage
        when: 'The user filters by an inexistnt user'
            filterByUserName("inexistentUser "+randStr)
        then: 'There are no rows returned'
            !rowsDisplayed()
    }

    def "2. New staff is can be filtered by name"() {
        given: 'A staff member exists and all filters are clear'
            at StaffListPage
            clearAllFilters()
        when: 'The user can be filtered by first name'
            filterByFirstName(firstName)
        then: 'The user is found'
            isExpectedUser(firstName,middleName,lastName,userEmail,userCompany)
    }

    def "3. User can be filtered by middle name"(){
        given: 'A staff member exists and all filters are clear'
            clearAllFilters()
        when: "The user flters by middle name"
            filterByMiddleName(middleName)
        then: "the user is found"
            isExpectedUser(firstName,middleName,lastName,userEmail,userCompany)
    }

    def "4. User can be filtered by last name"(){
        given: 'A staff member exists and all filters are clear'
            clearAllFilters()
        when: "The user flters by last name"
            filterByLastname(lastName)
        then: "the user is found"
            isExpectedUser(firstName,middleName,lastName,userEmail,userCompany)
    }

    def "5. User can be filtered by email address"(){
        given: 'A staff member exists and all filters are clear'
            clearAllFilters()
        when: "The user flters by last name"
            filterUserByEmail(userEmail)
        then: "the user is found"
            isExpectedUser(firstName,middleName,lastName,userEmail,userCompany)
    }
}

