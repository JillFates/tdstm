package specs.Admin.Staff

import geb.spock.GebReportingSpec
import pages.Admin.Staff.StaffListPage
import pages.Admin.User.UserListPage
import pages.Admin.User.UserDetailsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import geb.waiting.WaitTimeoutException

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
        def username = getRandomBaseUserNameByBaseName baseName
        filterByUsername username
        clickOnFirstUserName()
        try {
            at UserDetailsPage
            clickOnDeleteButtonAndConfirm()
            at UserListPage
            verifyDeletedMessage()
            usersToBeDeleted.add username
        } catch (WaitTimeoutException e) {
            assert isUserDeleted() // userLogin could be deleted by other test running in parallel
        }
    }

    def deleteStaff(){
        usersToBeDeleted.each {
            filterByLastname it
            if (!verifyNoRecordsDisplayed()){ // staff could be deleted by other test running in parallel
                selectRow()
                clickOnBulkDeleteButton()
                clickOnDeleteAssociatedAppOwnerOrSMEsInput()
                clickOnDeleteConfirmationModalButton()
                clickOnCloseConfirmationModalButton()
                at StaffListPage // need to wait because page is refreshed after deleting a staff member
            }
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
                at StaffListPage
                filterByLastname baseName
                getGridRowsSize() >= maxNumberOfUsers
                println "We are still good, no staff needs to be deleted."
            }
    }
}