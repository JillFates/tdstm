package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.AssetViewManager.AssetViewsPage
import pages.AssetViewManager.ViewPage
import pages.AssetViewManager.SaveViewPage
import utils.CommonActions


@Stepwise
class ViewManagerSharedViewsSpec extends GebReportingSpec {

    def testKey
    static testCount
    static numberOfRows
    static favView
    //Define the names of the Application you will Create and Edit
    static randStr = CommonActions.getRandomString()

    static viewName= "QAE2E " +randStr+" Shared"
    static  initValue=""
    static user=2, pass=3

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { assetsModule.goToAssetViewManager() }
        at AssetViewsPage
    }

    def "1. Validates the listed views in Shared Views are correct"(){
        given: "I am in the My Views section"
            at AssetViewsPage
        when : "I go to Shared views"
            waitFor {viewMgrSharedViews.click()}
        then: "All of the views displayed are shared views"
            allViewsModule.validateIsShared()
    }

    def "2. Validate shared view can be viewed by a different user"() {
        given: "User is in View Manager"
            at AssetViewsPage
        when: "The user goes to Shared Views"
            waitFor {viewMgrSharedViews.click()}
        then: "The counter on the left and the number of rows match"
            validateSharedViewsCount()
    }

    def "3. User shares view (edit) and it is added to Shared Views list"() {
        given: "User is in All views list"
            at AssetViewsPage
            goToMyViews()
            initValue=getSharedCounter()
        when: "User sets a view as shared view"
            //need to search for one that has not been shared yet and click on it
            allViewsModule.clickOnNonSharedView()
            at ViewPage
            waitFor{clickOnGear()}
            createViewModule.clickSaveAs()
            at SaveViewPage
            enterName(viewName)
            setViewAsShared()
            at ViewPage
            clickViewManagerBreadCrumb()
            at AssetViewsPage
            goToSharedViews()
        then: "The view is now listed in Shared Views"
            allViewsModule.validateRowNames(viewName, true)
        and: "The Shared Views counter has been incremented in one"
            validateValueIncrement(initValue, getSharedCounter())
    }

    def "4. A different user is able to see the view now that it´s shared"() {
        given: "User is in Asset View Page"
            at AssetViewsPage
        when: "User logs out and logs in with different credentials"
            to LoginPage
            at LoginPage
            login(user,pass)
            at MenuPage
            waitFor {assetsModule.goToAssetViewManager()}
            at AssetViewsPage
        then: "The view is visible to the current user"
            waitFor{ allViewsModule.validateRowNames(viewName, true)}
    }
}
