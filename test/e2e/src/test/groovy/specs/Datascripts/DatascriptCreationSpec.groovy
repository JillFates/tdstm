package specs.Datascripts
import pages.Datascripts.DatascriptsPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage
import jodd.util.RandomString
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class DatascriptCreationSpec extends GebReportingSpec{

    def testKey
    static testCount
    static randStr =  RandomString.getInstance().randomAlphaNumeric(4) + " "
    static E2E = "E2E Datascript"


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


    def "1. The user navigates to the Providers Section"() {
        testKey = "TM-XXXX"
        given: 'The User landed on the Menu Page after login'
            at MenuPage
        when: 'The user goes to the Datascripts page'
            menuModule.goToDatascripts()

        then: 'The Providers Page loads with no problem'
            at DatascriptsPage
    }

    def "2. Open the Create Datascripts Page"() {
        testKey = "TM-XXXX"
        given: 'The user is on the Datascript landing page'
            at DatascriptsPage
        when: 'The user clicks the Create Datascripts Button'
            createBtn.click()

        then: 'The Create Datascripts pop up loads with no problem'
            //at
    }

}
