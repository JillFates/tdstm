package specs.Assets.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.Assets.AssetViewManager.AssetViewsPage
import pages.Assets.AssetViews.ViewPage
import utils.CommonActions

@Stepwise
class ViewManagerListSpec extends GebReportingSpec {

    def testKey
    static testCount
    static numberOfRows
    //Define the names of the Application you will Create and Edit
    static randStr = CommonActions.getRandomString()
    static viewName=  "QAE2E " +randStr+" View"
    def filteredName=""
    static listAll=["All Assets"]
    def listValidation=true

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        waitFor { assetsModule.goToAssetViewManager() }
    }
    def "1. Validate section title and presence of 'All Views'"() {
        given: "I am in Asset Views Page"
            at AssetViewsPage
        when: "I click on All Views"
            waitFor {viewMgrAllViews.click()}
        then: "I see at least the All Assets View"
            allViewsModule.validateRowNames("All Assets", true)
        and: "The title on the section is correct"
            allViewsModule.moduleTitleIsCorrect("All")
    }
    def "2. Validates the listed views in Favorites are correct"(){
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
        given: "I am in the Favorites section"
            at AssetViewsPage
        when : "I go to My Views"
            waitFor {viewMgrMyViews.click()}
        then: "Only the user's views are displayed"
            allViewsModule.validateAuthor()
        and: "The title on the section is correct"
            allViewsModule.moduleTitleIsCorrect("My Views")
    }
    def "4. Validates the listed views in Shared Views are correct"(){
        given: "I am in the My Views section"
            at AssetViewsPage
        when : "I go to Shared views"
            waitFor {viewMgrSharedViews.click()}
        then: "Only the user's shared views are displayed"
            allViewsModule.validateIsShared()
        and: "The title on the section is correct"
            allViewsModule.moduleTitleIsCorrect("Shared Views")
    }
    def "5. Validate views can be filtered by name"(){
        given: "User goes to All views and there is a certain number of rows"
            waitFor {viewMgrAllViews.click()}
            numberOfRows=allViewsModule.getNumberOfRows()
        when : "User filters views by name"
            allViewsModule.filterViewByName "All Assets"
        then: "Only views with names containing the text are listed"
            allViewsModule.validateRowNames("All Assets", false)
    }
    def "6. Validate filter is correctly removed"(){
        given: "User had previously filtered views by name"
            allViewsModule.filterViewByName "All Assets"
        when : "User clears the filter"
            allViewsModule.clearFilterViewByName()
        then: "The original number of rows is displayed again"
            allViewsModule.allRowsAreBack(numberOfRows)
    }


    def "7. Validates the listed views in System Views are correct"(){
        given: "I am in the Shared Views section"
            at AssetViewsPage
        when : "I go to System Views"
            waitFor {viewMgrSystemViews.click()}
        then: "Only the System views are displayed"
            allViewsModule.systemViewsOnly()
        and: "The title on the section is correct"
            allViewsModule.moduleTitleIsCorrect("System Views")
    }
    def "8. Erase icon is reactive"(){
        given: "I am in the System Views section"
            at AssetViewsPage
        when: "I go to My views and hit the erase button"
            goToMyViews()
            waitFor { allViewsModule.clickOnFirstDelete()}
        then: "I am prompted to confirm the deletion"
            allViewsModule.confirmationRequiredIsDisplayed()
    }
    def "9. Validate user can click on a view and then go back to the list"(){
        given: "I am in the System Views section"
            at AssetViewsPage
        when: "I click in the first view of the list and then on View Manager breadcrumb"
            waitFor { allViewsModule.clickFirstViewOfTheList()}
            at ViewPage
            waitFor {clickViewManagerBreadCrumb()}
        then: "I am back to the list of views"
            at AssetViewsPage
    }
}