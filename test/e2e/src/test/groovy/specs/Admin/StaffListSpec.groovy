package specs.Admin

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.Admin.StaffListPage
import pages.Admin.StaffCreationPage
import pages.Admin.UserCreationPage
import pages.Admin.UserDetailsPage
import pages.common.LoginPage
import pages.common.MenuPage
import spock.lang.Stepwise

@Stepwise
class StaffListSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr =  RandomString.getInstance().randomAlphaNumeric(4)

    //Define the names for the Staffq you will Create and Edit
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
        def username = "e2e_test_user"
        def password = "e2e_password"
        to LoginPage
        loginModule.login(username,password)
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    // Enter on Staff page
    def "Go to List Staff"() {
        testKey = "TM-XXXX"
        given:
        at MenuPage
        when:
        menuModule.goToAdminListStaff()
        then:
        at StaffListPage
        waitFor { gridSize > 0 }
    }

    def "Open Create Staff pop up"() {
        testKey = "TM-XXXX"
        given:
        at StaffListPage
        when:
        waitFor {createStaffBtn.click()}
        then:
        at StaffCreationPage
    }

    def "Create Staff"() {
        testKey = "TM-XXXX"
        given:
        at StaffCreationPage
        when:
        scModalFirstName = firstName
        scModalMiddleName = middleName
        scModalLastName = lastName
        scModalAddTeam.click()
        waitFor {scModalTeamSelector.present}
        scModalTeamSelector = teamName
        waitFor { scModalSaveBtn.click() }

        then:
        at StaffListPage
        waitFor {pageMessage.text() == "A record for "+firstName+" "+middleName+" "+lastName+" was created"}
    }

    def "Filter by Staff First Name" () {
        testKey = "TM-XXXX"
        given:
        at StaffListPage
        when:
        waitFor { firstNameFilter.click() }
        firstNameFilter = firstName
        then:
        waitFor{$("td", "role": "gridcell", "aria-describedby": "personIdGrid_firstname").find("a").text() == firstName}
    }

    def "Open Create UserLogin Page" () {
        testKey = "TM-XXXX"
        given:
        at StaffListPage
        when:
        waitFor{$("td","role":"gridcell",title:" Create User").find("a").click()}
        then:
        at UserCreationPage
    }

    def "Create User"() {
        testKey = "TM-XXXX"
        given:
        at UserCreationPage
        when:
        ucUsername = userName
        ucEmail = userEmail
        waitFor {ucPassword.click()}
        ucPassword = userPass
        waitFor { ucConfirmPassword.click()}
        ucConfirmPassword = userPass
        ucProjectSelector = userProject
        ucAdminRoleCB.value(true)
        waitFor { ucSaveBtn.click() }

        then:
        at UserDetailsPage
        waitFor {pageMessage.text() == "UserLogin "+userName+" created"}
    }

}


