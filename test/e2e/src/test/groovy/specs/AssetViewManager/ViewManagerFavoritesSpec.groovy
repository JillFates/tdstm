package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.AssetViewManager.AssetViewsPage
import pages.AssetViewManager.ViewPage
import jodd.util.RandomString


@Stepwise
class ViewManagerFavoritesSpec extends GebReportingSpec {

    def testKey
    static testCount
    static numberOfRows
    //Define the names of the Application you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "TM8503"
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
    def "1. Validate User can reach Favorite Views"() {
        testKey = "TM-8503"
        given: "I am in Asset Views Page"
            at AssetViewsPage
        when: "I click on Favorite Views"
            waitFor {goToFavourites()}
        then: "I see at least the Favorites section"
            allViewsModule.moduleTitleIsCorrect("Favorites")
        and: "The list is propely populated"
            allViewsModule.numberViewNamesEqualsNumberRows()
            allViewsModule.numberEditButtonsEqualsNumberRows()
            allViewsModule.createdDateNotEmpty()
        and: "The all Star icons are ON"
            allViewsModule.noVoidStarsAreDisplayed()
    }
    /*
    def "2. Validates adding Favorites increases the counter on the left"(){
        testKey = "TM-8503"
        given: "a"
            at AssetViewsPage
        when : "I go to favorites"
            waitFor {goToFavourites()}
        then: "a"
            allViewsModule.noVoidStarsAreDisplayed()
        and: "a"
            allViewsModule.moduleTitleIsCorrect("Favorites")
    }
    def "3. Validate star is off in non-fav view"(){
        testKey = "TM-8503"
        given: "a"
            at AssetViewsPage
        when : "a"
            waitFor {viewMgrMyViews.click()}
        then: "a"
            allViewsModule.validateAuthor()
        and: "a"
            allViewsModule.moduleTitleIsCorrect("My Views")
    }
    def "4. Validates view just dded as fav is listed in all"(){
        testKey = "TM-8503"
        given: "n"
            at AssetViewsPage
        when : "qs"
            waitFor {viewMgrSharedViews.click()}
        then: "q"
            allViewsModule.validateIsShared()

    }
    def "5. Validate views just fav´d is liste din favs"(){
        testKey = "TM-8503"
        given: "s"
            waitFor {viewMgrAllViews.click()}
            numberOfRows=allViewsModule.getNumberOfRows()
        when : "Ume"
            allViewsModule.filterViewByName "All Assets"
        then: "d"
            allViewsModule.validateRowNames("All Assets", false)
    }
    */
}