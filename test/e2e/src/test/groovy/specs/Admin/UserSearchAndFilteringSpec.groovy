package specs.Admin

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Admin.StaffListPage
import pages.Admin.StaffCreationPage
import pages.Admin.UserCreationPage
import pages.Admin.UserDetailsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.Admin.UserListPage

/**
 * @author ingrid
 */

@Stepwise
class UserSearchAndFilteringSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()

    //Define the names for the Staff that you will Create and Edit
    static baseName = "QAE2E"
    static userProject = "TM-Demo"
    static teamName = "Account Manager"
    static firstName = baseName + randStr + "First"
    static middleName = baseName + randStr + "Middle"
    static lastName = baseName + randStr + "Last"
    static userName = baseName + randStr + "Test"
    static userPass = baseName + randStr + "Pass*"
    static userEmail = baseName + randStr + "testuser@transitionaldata.com"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        /**
         * the following code is part of the setup in order to create the data
         * that will be validated
         */
        adminModule.goToAdminListStaff()
        at StaffListPage
        waitFor {createStaffBtn.click()}
        at StaffCreationPage
        scModalFirstName = firstName
        scModalMiddleName = middleName
        scModalLastName = lastName
        scModalAddTeam.click()
        waitFor {scModalTeamSelector.present}
        scModalTeamSelector = teamName
        waitFor { scModalSaveBtn.click() }
        at StaffListPage
        firstNameFilter = firstName
        waitFor{$("td","role":"gridcell",title:" Create User").find("a").click()}
        at UserCreationPage
        ucUsername = userName
        ucEmail = userEmail
        waitFor {ucPassword.click()}
        ucPassword = userPass
        waitFor { ucConfirmPassword.click()}
        ucConfirmPassword = userPass
        waitFor { ucConfirmPassword.click()}
        ucConfirmPassword = userPass
        ucProjectSelector = userProject
        ucAdminRoleCB.value(true)
        waitFor { ucSaveBtn.click() }
        at UserDetailsPage
        adminModule.goToListUsers()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. The User Navigates in the Staff List Section"() {
        given: 'The User is at Menu Page'
            at MenuPage
        when: 'The User Clicks in the Admin > List Users Menu Option'
            adminModule.goToListUsers()
        then: 'User List Should be displayed'
            at UserListPage
    }

    def "2. The user peforms search for inexistent user"(){
        given: 'The user is at User List page'
            at UserListPage
        when: 'The user filters by an inexistnt user'
            filterByUsername("inexistentUser "+randStr)
        then: 'There are no rows returned'
            !rowsDisplayed()
    }

    def "3. New staff and user are created and filters can be applied to find the user"() {
        given: 'A staff member is created, and a user for it'

            at UserListPage
        when: 'The user just created is filtered by userName'
            filterByUsername(userName)
        then: 'The user is found'
            isExpectedUser(userName,firstName,lastName)
    }

    def "4. User can be filtered by name"(){
        when: "The user flters by first name"
            filterByPerson(firstName)
        then: "the user is found"
            isExpectedUser(userName,firstName,lastName)
    }

    def "5. User can be filtered by last name"(){
        when: "The user flters by last name"
            filterByPerson(lastName)
        then: "the user is found"
            isExpectedUser(userName,firstName,lastName)
    }
}
