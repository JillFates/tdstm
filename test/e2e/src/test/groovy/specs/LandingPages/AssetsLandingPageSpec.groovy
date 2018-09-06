package specs.LandingPages

import geb.spock.GebReportingSpec

import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Assets.*
import pages.AssetViewManager.*
import spock.lang.Stepwise

/**
 * This class sweeps over the Assets Menu.
 * It checks every single landing page.
 * @author alvaro
 */

@Stepwise
class AssetsLandingPageSpec extends GebReportingSpec {
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

    def "1. The Assets Menu has the correct number of elements"() {
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The User Clicks on the Assets Menu'
            assetsModule.goToAssetsMenu()

        then: 'The valid clickable pages are displayed'
            assetsModule.assetsPages.size() == 18
            at MenuPage
    }

    def "2. Verify the Summary Table Landing Page"() {
        when: 'The user goes to the Summary Table page'
            assetsModule.goToSummaryTable()

        then: 'The Summary Table Page is loaded successfully'
            at SummaryTablePage
    }

    def "3. Verify the All Assets Landing Page"() {
        when: 'The user goes to the All Assets page'
            assetsModule.goToAllAssets()

        then: 'The All Assets Page is loaded successfully'
            at ViewPage
    }

    def "4. Verify the Applications Landing Page"() {
        when: 'The user goes to the Applications page'
            assetsModule.goToApplications()

        then: 'The Applications Page is loaded successfully'
            at ApplicationListPage
    }

    def "5. Verify the Devices Landing Page"() {
        when: 'The user goes to the Devices page'
            assetsModule.goToDevices()

        then: 'The Devices Page is loaded successfully'
            at DevicesPage
    }

    def "6. Verify the Servers Landing Page"() {
        when: 'The user goes to the Servers page'
            assetsModule.goToServers()

        then: 'The Servers Page is loaded successfully'
            at ServersPage
    }

    def "7. Verify the Databases Landing Page"() {
        when: 'The user goes to the Databases page'
            assetsModule.goToDatabases()

        then: 'The Databases Page is loaded successfully'
            at DatabasesPage
    }

    def "8. Verify the Storage Devices Landing Page"() {
        when: 'The user goes to the Databases page'
            assetsModule.goToStorageDevices()

        then: 'The Storage Devices Page is loaded successfully'
            at StorageDevicesPage
    }

    def "9. Verify the Storage Logical Landing Page"() {
        when: 'The user goes to the Databases page'
            assetsModule.goToStorageLogical()

        then: 'The Storage Logical Page is loaded successfully'
            at StorageLogicalPage
    }

    def "10. Verify the Dependencies Landing Page"() {
        when: 'The user goes to the Dependencies page'
            assetsModule.goToDependencies()

        then: 'The Dependencies Page is loaded successfully'
            at DependenciesPage
    }

    def "11. Verify the Comments Landing Page"() {
        when: 'The user goes to the Comments page'
            assetsModule.goToComments()

        then: 'The Comments Page is loaded successfully'
            at CommentsPage
    }

    def "12. Verify the Dependency Analyzer Landing Page"() {
        when: 'The user goes to the Dependency Analyzer page'
            assetsModule.goToDependencyAnalyzer()

        then: 'The Dependency Analyzer Page is loaded successfully'
            at DependencyAnalyzerPage
    }

    def "13. Verify the Architecture Graph Landing Page"() {
        when: 'The user goes to the Dependency Analyzer page'
            assetsModule.goToArchitectureGraph()

        then: 'The Architecture Graph Page is loaded successfully'
            at ArchitectureGraphPage
    }

    def "14. Verify the Export Assets Landing Page"() {
        when: 'The user goes to the Export Assets page'
            assetsModule.goToAssetExport()

        then: 'The Export AssetsPage is loaded successfully'
            at AssetExportPage
    }

    def "15. Verify the Import Assets ETL Landing Page"() {
        when: 'The user goes to the Import Assets ETL page'
            assetsModule.goToAssetImportETL()

        then: 'The Import Assets ETL is loaded successfully'
            at AssetImportETLPage
    }

    def "16. Verify the Import Assets Excel Landing Page"() {
        when: 'The user goes to the Import Assets Excel page'
            assetsModule.goToAssetImportExcel()

        then: 'The Import Assets Excel is loaded successfully'
            at AssetImportExcelPage
    }

    def "17. Verify the Manage Import Batches ETL Landing Page"() {
        when: 'The user goes to the Manage Import Batches ETL page'
            assetsModule.goToManageImportBatchETL()

        then: 'The Manage Import Batches ETL is loaded successfully'
            at ManageImportBatchesETLPage
    }

    def "18. Verify the Manage Import Batches Excel Landing Page"() {
        when: 'The user goes to the Manage Import Batches Excel page'
            assetsModule.goToManageImportBatchExcel()

        then: 'The Manage Import Batches Excel is loaded successfully'
            at ManageImportBatchesExcelPage
    }

    def "19. Verify the View Manager Landing Page"() {
        when: 'The user goes to the View Manager page'
            assetsModule.goToAssetViewManager()

        then: 'The Manage View Manager is loaded successfully'
            at AssetViewsPage
    }



}