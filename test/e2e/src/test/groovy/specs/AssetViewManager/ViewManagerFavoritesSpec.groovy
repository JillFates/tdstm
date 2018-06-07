package specs.AssetViewManager

import geb.spock.GebReportingSpec
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import pages.AssetViewManager.AssetViewsPage
import pages.AssetViewManager.ViewPage
import pages.AssetViewManager.SaveViewPage
import jodd.util.RandomString


@Stepwise
class ViewManagerFavoritesSpec extends GebReportingSpec {

    def testKey
    static testCount
    static numberOfRows
    static favView
    //Define the names of the Application you will Create and Edit
    static randStr =  RandomString.getInstance().randomAlphaNumeric(6)
    static baseName = "TM8503"
    static viewName=  randStr+" "+baseName
    static minimumNumberOfRows =11


    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage

        waitFor { menuModule.goToAssetViewManager() }
        at AssetViewsPage
        /**
         * The existence of at least 11 views is a precondition to
         * this spec.
         */
        int rowCount= allViewsModule.getNumberOfRows()
        if (rowCount<minimumNumberOfRows) {
            int i=0
            while (i < (minimumNumberOfRows-rowCount)) {
                allViewsModule.getNumberOfRows()
                waitFor { allViewsModule.clickCreateView() }
                waitFor { createViewModule.selectApplication() }
                waitFor { createViewModule.clickNext() }
                createViewModule.selectRandomCheckboxes()
                waitFor { createViewModule.clickSave() }
                at SaveViewPage
                waitFor { enterName(RandomString.getInstance().randomAlphaNumeric(10)) }
                waitFor { clickSave() }
                waitFor { menuModule.goToAssetViewManager() }
                at AssetViewsPage
                i++
            }
        }
    }
    //count number of viwes
    def "1. Validate Favorite number limit pop up is diplayed"() {
        testKey = "TM-10911"
        given: "User is in Asset Views Page"
            at AssetViewsPage
        when: "User gets the max Favs popup"
            //Count existing number of fav views if any
            //add fav views until number reaches ten,
            getFavLimitPopUp()
            // next addition shuoild display the pop up
        then: "The message is as expected"
            favLimitPopUpTextIsAsExpected()
    }

    def "2. Validate there are actually 10 starred views"() {
        testKey = "TM-10911"
        given: "User is in Asset Views Page and the limit pop up is displayed"
            at AssetViewsPage
            favLimitPopUpIsPresent()
            allViewsModule.validateNumberOfStarredViews(10)
        when: "User closes the pop up"
            closeFavLimitPopUp()
        then: "Ten views are starred"
            allViewsModule.validateNumberOfStarredViews(10)
    }

    def "3. Reducing the number of favorites and increasing it will again cause the pop up to display"() {
        testKey = "TM-10911"
        given: "User is in Asset Views Page and there are ten favorite views"
            at AssetViewsPage
        when: "The user decreases and again adds favorite views"
            allViewsModule.unFavRandomFavs()
            getFavLimitPopUp()
        then: "The pop up is displayed once more"
            waitFor{favLimitPopUpIsPresent()}
    }

    def "4. Validate User can reach Favorite Views"() {
        testKey = "TM-8503"
        given: "I am in Asset Views Page and the limit of favorites has not been reached"
            at AssetViewsPage
            closeFavLimitPopUp()
            allViewsModule.unFavRandomFavs()
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

    def "5. Validates adding Favorites increases the counter on the left"(){
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

    def "6. Validate star is off in non-fav view"(){
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

    def "7. Validates that the view just added as fav is listed in all"(){
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