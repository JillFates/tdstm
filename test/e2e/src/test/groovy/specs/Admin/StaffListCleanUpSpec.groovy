package specs.Admin

import geb.spock.GebReportingSpec
import pages.Admin.StaffListPage
import pages.Admin.UserListPage
import pages.Admin.UserDetailsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class StaffListCleanUpSpec extends GebReportingSpec {
    def testKey
    static testCount
    static baseName = "QAE2E"
    static maxNumberOfUsers = 1
    static maxNumberOfUsersToBeDeleted = 2
    static usersToBeDeleted = []

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        adminModule.goToListUsers()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def deleteUserLoginAccount(){
        usersToBeDeleted.add getFirstUserLastNameDisplayed()
        clickOnFirstUserName()
        at UserDetailsPage
        clickOnDeleteButtonAndConfirm()
        at UserListPage
        verifyDeletedMessage()
    }

    def deleteStaff(){
        usersToBeDeleted.each {
            filterByLastname it
            selectRow()
            clickOnBulkDeleteButton()
            clickOnDeleteAssociatedAppOwnerOrSMEsInput()
            clickOnDeleteConfirmationModalButton()
            clickOnCloseConfirmationModalButton()
            at StaffListPage // need to wait because page is refreshed after deleting a staff member
        }
    }

    def '1. The User deletes Staff starting with QAE2E for clean up purposes'(){
        given: 'The user is in User List page'
            at UserListPage
        when: 'The user filters by Username starting with QAE2E'
            filterByUsername baseName
        then: 'The user deletes users if there are more than specified number of users'
            def count = 0
            while (getGridRowsSize() > maxNumberOfUsers){
                count = count + 1
                if (count > maxNumberOfUsersToBeDeleted) {
                    break
                }
                deleteUserLoginAccount()
                at UserListPage
                filterByUsername baseName
            }
            getGridRowsSize() >= maxNumberOfUsers
        and: 'The user deletes staff if there were login user accounts deleted'
            if (!usersToBeDeleted.isEmpty()) {
                at MenuPage
                adminModule.goToAdminListStaff()
                at StaffListPage
                deleteStaff()
                at StaffListPage
                filterByLastname baseName
                getGridRowsSize() >= maxNumberOfUsers
                println "$count user/staff were deleted."
            } else {
                at MenuPage
                adminModule.goToAdminListStaff()
                filterByLastname baseName
                getGridRowsSize() >= maxNumberOfUsers
                println "We are still good, no staff needs to be deleted."
            }
    }
}