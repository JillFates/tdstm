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

    @Stepwise
class StaffListSpec extends GebReportingSpec {
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
    static userPass = baseName + randStr + "Pass*"
    static userEmail = baseName + randStr + "testuser@transitionaldata.com"
    static originalDetails = [userCompany,firstName,middleName,lastName,userName,userEmail]
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

    def "1. The User Navigates in the Staff List Section"() {
        given: 'The User searches in the Menu Page'
            at MenuPage
        when: 'The User Clicks in the Admin > List Staff Menu Option'
            adminModule.goToAdminListStaff()

        then: 'Staff List Should be displayed'
            at StaffListPage
        and: 'We wait for the Entire in order to be properly displayed'
            waitFor { gridSize > 0 }
    }

    def "2. The User gains access to the Create Staff Pop-up"() {
        given: 'The User is on the Staff List Page'
            at StaffListPage
        when: 'The User Clicks the "Create Staff" Button'
            waitFor {createStaffBtn.click()}

        then: 'Create Staff Pop-up should be displayed'
            at StaffCreationPage
    }

    def "3. A brand new Staff is successfully created"() {
        given: 'The User is in the Create Staff Pop-up'
            at StaffCreationPage
        when: 'The User randomly completes First, Middle and Last Name'
            scModalFirstName = firstName
            scModalMiddleName = middleName
            scModalLastName = lastName
        and: 'The User Clicks the "Add Team" Option'
            scModalAddTeam.click()
        and: 'We wait for the Selector to be present'
            waitFor {scModalTeamSelector.present}
        and: 'The User Searches by the "Account Manager" Team Member'
            scModalTeamSelector = teamName
        and: 'The User Clicks the "Save" Button'
            waitFor { scModalSaveBtn.click() }
        then: 'The Pop-up should be closed and the User should be redirected to the Staff List Page'
            at StaffListPage
        and: 'A Success massage stating the User that was currently created should be displayed'
            waitFor {pageMessage.text() == "A record for "+firstName+" "+middleName+" "+lastName+" was created"}
    }

    def "3. The User Filters out by Staff First Name that was recently created"() {
        given: 'The User is on the Staff List Page'
            at StaffListPage
        when: 'The User Clicks the First Name Filter Column'
            waitFor { firstNameFilter.click() }
        and: 'The User enters the First Name'
            firstNameFilter = firstName

        then: 'The User should be found'
            waitFor{$("td", "role": "gridcell", "aria-describedby": "personIdGrid_firstname").find("a").text() == firstName}
    }

    def "4. The User gains access to the Create UserLogin Section"() {
        given: 'The User is on the Staff List Page'
            at StaffListPage
        when: 'The User Searches by and clicks the "Create User" Button'
            waitFor{$("td","role":"gridcell",title:" Create User").find("a").click()}

        then: 'The User is redirected to the UserLogin Section'
            at UserCreationPage
    }

    def "5. A brand New User is successfully created"() {
        given: 'The User is on the Create UserLogin Section'
            at UserCreationPage
        when: 'The User Completes his Username, Email'
            ucUsername = userName
            ucEmail = userEmail
        and: 'The User Completes the Password'
            waitFor {ucPassword.click()}
            ucPassword = userPass
        and: 'The User Confirms the Password'
            waitFor { ucConfirmPassword.click()}
            ucConfirmPassword = userPass
        and: 'The User Selects The Project'
            ucProjectSelector = userProject
        and: 'A random number of roles are assigned to the user'
            selectRandomRoles()
        and: 'The User clicks the "Save" Button'
            waitFor { ucSaveBtn.click() }
        then: 'The User should be redirected to the User List Section'
            at UserDetailsPage
        and: 'A success message related to the User that was created should be displayed'
            waitFor {pageMessage.text() == "UserLogin "+userName+" created"}
        and: 'All of the details entered when creating the user are displayed'
            validateUserDetails(originalDetails)
    }
}