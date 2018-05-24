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
    static favView
    //Define the names of the Application you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)
    static baseName = "TM8503"
    static viewName=  randStr+" "+baseName


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
        and: "The list is properly populated"
            allViewsModule.numberViewNamesEqualsNumberRows()
            allViewsModule.numberEditButtonsEqualsNumberRows()
            allViewsModule.createdDateNotEmpty()
        and: "All Star icons are ON"
            allViewsModule.noVoidStarsAreDisplayed()
    }

    def "2. Validates adding Favorites increases the counter on the left"(){
        testKey = "TM-8503"
        given: "The user is in the View Manager Page"
            waitFor {goToAllViews()}
            at AssetViewsPage
        when : "Ths user sets a view as favorite"
            def initialFavAmount= getFavCounter()
            waitFor {allViewsModule.setFirstNonFavViewAsFav()}
        then: "The counter is incremented"
            waitFor{validateValueIncrement(initialFavAmount,getFavCounter())}
    }

    def "3. Validate star is off in non-fav view"(){
        testKey = "TM-8503"
        given: "The user is in the View Manager Page"
            at AssetViewsPage
        when : "The user clicks on a non favorited view"
            favView=allViewsModule.getNameOfFirstNonFavView()
            allViewsModule.goToFirstNonFavView()
            at ViewPage
        then: "The view's star is disabled"
            validateStarIsOff()
    }

    def "4. Validates that the view just added as fav is listed in all"(){
        testKey = "TM-8503"
        given: "The user is in the View page"
            at ViewPage
        when : "The user clicks on the star"
            setViewAsFavorite()
        then: "The view is still listed in All views"
            waitFor{clickViewManagerBreadCrumb()}
            at AssetViewsPage
            waitFor{allViewsModule.validateRowNames(favView,true)}
        and: "The view is also listed in Favorite views"
            waitFor {goToFavourites()}
            waitFor{allViewsModule.validateRowNames(favView, true)}
    }
}