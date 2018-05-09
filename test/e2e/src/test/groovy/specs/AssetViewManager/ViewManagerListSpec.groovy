package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.AssetViewManager.AssetViewsPage
import jodd.util.RandomString


@Stepwise
class ViewManagerListSpec extends GebReportingSpec {

    def testKey
    static testCount

    //Define the names of the Application you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "TM8501"
    static viewName=  randStr+" "+baseName
    def filteredName=""
    static listAll=["All Assets"]
    def listValidation=true

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { menuModule.goToAssetViewManager() }
    }
    def "1. validates user can reach 'my views' "() {
        testKey = "TM-8501"
        given: "I am in Asset Views Page"
            at AssetViewsPage
        when: "I click on All Views"
            waitFor {viewMgrAllViews.click()}
        then: "I see at least the All Assets View"
            allViewsModule.validateViewIsListed("All Assets")
        and: "The title on the section is correct"
            allViewsModule.moduleTitleIsCorrect("All")
    }
    def "2. Validates the listed views in Favorites are correct"(){
        testKey = "TM-8501"
        given: "I am in the All views section"
            at AssetViewsPage
        when : "I go to favorites"
            waitFor {goToFavourites()}
        then: "Only the user favorite views are displayed"
            allViewsModule.noVoidStarsAreDisplayed()
        and: "The title on the section is correct"
            allViewsModule.moduleTitleIsCorrect("Favorites")
    }
    def "3. Validates the listed views in My Views are correct"(){
        testKey = "TM-8501"
        given: "I am in the Favorites section"
            at AssetViewsPage
            allViewsModule.displayed
        when : "I go to My Views"
            waitFor {viewMgrMyViews.click()}
        then: "Only the user's views are displayed"
            allViewsModule.validateAuthor()
        and: "The title on the section is correct"
            allViewsModule.moduleTitleIsCorrect("My Views")
    }
    def "4. Validates the listed views in Shared Views are correct"(){
        testKey = "TM-8501"
        given: "I am in the My Views section"
        at AssetViewsPage
        allViewsModule.displayed
        when : "I go to Shared views"
        waitFor {viewMgrSharedViews.click()}
        then: "Only the user's shared views are displayed"
        allViewsModule.validateIsShared()
        and: "The title on the section is correct"
        allViewsModule.moduleTitleIsCorrect("Shared Views")
    }
    def "5. Validates the listed views in System Views are correct"(){
        testKey = "TM-8501"
        given: "I am in the Shared Views section"
        at AssetViewsPage
        allViewsModule.displayed
        when : "I go to System Views"
        waitFor {viewMgrSystemViews.click()}
        then: "Only the Sytem views are displayed"
        allViewsModule.systemViewsOnly()
        and: "The title on the section is correct"
        allViewsModule.moduleTitleIsCorrect("System Views")
    }
    def "6. Erase icon is reactive"(){
        testKey = "TM-8501"
        given: "I am in the System Views section"
        at AssetViewsPage
        when: "I go to My views and hit the erase button"
        goToMyViews()
        waitFor { allViewsModule.clickOnFirstDelete()}
        then: "I am prompted to confirm the deletion"
        allViewsModule.confirmationRequiredIsDisplayed()
    }
}