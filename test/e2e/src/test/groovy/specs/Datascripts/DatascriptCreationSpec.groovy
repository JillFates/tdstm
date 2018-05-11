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


}
