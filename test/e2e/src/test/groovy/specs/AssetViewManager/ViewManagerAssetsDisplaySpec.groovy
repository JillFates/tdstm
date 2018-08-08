package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.AssetViewManager.AssetViewsPage
import pages.Assets.NewAssetDetailsPage
import pages.AssetViewManager.ViewPage
import jodd.util.RandomString


@Stepwise
class ViewManagerAssetsDisplaySpec extends GebReportingSpec {

    def testKey
    static testCount
    static selectedAsset=""
    static rowData= []

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { menuModule.goToAssetViewManager() }
        at AssetViewsPage
    }

    def "1. validate I can open a View"(){
        given: "I am in All views section"
            at AssetViewsPage
        when: "I click on All Assets view"
            allViewsModule.filterViewByName("All Assets")
            allViewsModule.openViewByName("All Assets")
        then: "I am taken to the view's page"
            at ViewPage
    }

    def "2. Validate user can open an asset by clicking on its name"() {
        given: "Assets are listed for the view"
            at ViewPage
        when: "The user clicks on the asset´s name"
            rowData= clickRandomAssetName()
            selectedAsset=rowData.getAt(0)
        then: "Asset´s details are displayed"
            at NewAssetDetailsPage
        and: "The name or ID displayed in the screen matches the one clicked"
            (selectedAsset==getName())
    }

    def "3. Validate all row Data is displayed"() {
        given: "Assets are listed for the view"
            at NewAssetDetailsPage
        when: "The Asset details are displayed"
            adModalWindow.displayed
            def dataDisplayed=getContent()
        then: "All the data in the row is present"
            validateDataIsPresent(rowData, dataDisplayed)
    }
}
