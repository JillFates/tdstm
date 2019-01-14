package specs.LandingPages

import geb.spock.GebReportingSpec
// Useful for importing the Menu as part of the Landing Pages Inspection
import pages.Admin.*
import pages.Admin.Company.*
import pages.Admin.Staff.*
import pages.Admin.User.*
import pages.Admin.ImportAccounts.*
import pages.Admin.ExportAccounts.*
// Adding a new MenuItem
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise


@Stepwise
class AdminLandingPageSpec extends GebReportingSpec {
    def testKey
    static testCount

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

    def "1. The Admin Menu has the correct number of elements"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The User Clicks in the Admin > Menu'
            adminModule.goToAdminMenu()

        then: 'The number of elements displayed matches'
        //We set the number of elements as 25 because we need to take into account the divisions among items
        //We could also do the assertion comparing the length of the text
        //adminModule.adminMenu.text().length() == 268
            adminModule.adminMenu.children().size() == 25
            at MenuPage
    }

    def "2. Verify the Admin Portal Landing Page"() {
        testKey = "TM-XXXX"
        given: 'The user is on the initial page'
            at MenuPage
        when: 'The User Clicks in the Admin>Admin Portal option'
            adminModule.goToAdminPortal()

        then: 'We verify the AdminPortalPage loads fine'
            at AdminPortalPage
    }

    def "3. Verify the License Admin Landing Page"() {
        when: 'The User Clicks in the Admin>License Admin option'
            adminModule.goToLicenseAdmin()

        then: 'We verify the AdminPortalPage loads fine'
            at LicensePage
    }

    def "4. Verify the Notices Landing Page"() {
        when: 'The User Clicks in the Admin>Notices option'
            adminModule.goToNoticesAdmin()

        then: 'We verify the Notice Administration page loads fine'
            at NoticePage
    }

    def "5. Verify the Role Permissions Landing Page"() {
        when: 'The User Clicks in the Admin>Role Permissions option'
            adminModule.goToRolePermissions()

        then: 'We verify the Role Permissions page loads fine'
            at RolePermissionPage
    }

    def "6. Verify the Asset Options Landing Page"() {
        when: 'The User Clicks in the Admin>Asset Options option'
            adminModule.goToAssetOptions()

        then: 'We verify the Asset Options page loads fine'
            at AssetOptionsPage
    }

    def "7. Verify the List Companies Landing Page"() {
        when: 'The User Clicks in the Admin>List Companies option'
            adminModule.goToListCompanies()

        then: 'We verify the List Companies page loads fine'
            at ListCompaniesPage
    }

    def "8. Verify the List Staff Landing Page"() {
        when: 'The User Clicks in the Admin>List Staff option'
            adminModule.goToAdminListStaff()

        then: 'We verify the List Staff page loads fine'
            at StaffListPage
    }

    def "9. Verify the List Users Landing Page"() {
        when: 'The User Clicks in the Admin>List Users option'
            adminModule.goToListUsers()

        then: 'We verify the List Users page loads fine'
            at UserListPage
    }

    def "10. Verify the Import Accounts Landing Page"() {
        when: 'The User Clicks in the Admin>Import Accounts option'
            adminModule.goToImportAccounts()

        then: 'We verify the Import Accounts page loads fine'
            at ImportAccountsPage
    }

    def "11. Verify the Export Accounts Landing Page"() {
        testKey = "TM-XXXX"
        when: 'The User Clicks in the Admin>Export Accounts option'
            adminModule.goToExportAccounts()

        then: 'We verify the Export Accounts page loads fine'
            at ExportAccountsPage
    }

    def "12. Verify the List Workflows Landing Page"() {
        when: 'The User Clicks in the Admin>List Workflows option'
            adminModule.goToListWorkflows()

        then: 'We verify the List Workflows page loads fine'
            at ListWorkflowsPage
    }

    def "13. Verify the List Manufacturers Landing Page"() {
        testKey = "TM-XXXX"
        when: 'The User Clicks in the Admin>List Workflows option'
            adminModule.goToListManufacturers()

        then: 'We verify the List Manufacturers page loads fine'
            at ListManufacturersPage
    }

    def "14. Verify the List Models Landing Page"() {
        when: 'The User Clicks in the Admin>List Models option'
            adminModule.goToListModels()

        then: 'We verify the List Models page loads fine'
            at ListModelsPage
    }

    def "15. Verify the Export Mfg & Models Landing Page"() {
        when: 'The User Clicks in the Admin>Export Mfg & Models option'
            adminModule.goToExportModels()

        then: 'We verify the Export Mfg & Models page loads fine'
            at ExportMfgModelsPage
    }

}
