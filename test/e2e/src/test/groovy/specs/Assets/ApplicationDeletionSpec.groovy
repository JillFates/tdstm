package specs.Assets

import geb.spock.GebReportingSpec
import pages.Assets.ApplicationDetailsPage
import pages.Assets.ApplicationEditionPage
import pages.Assets.ApplicationListPage
import pages.common.LoginPage
import pages.common.MenuPage
import spock.lang.Stepwise

@Stepwise
class ApplicationDeletionSpec extends GebReportingSpec{

    def testKey
    static testCount
    static filterPattern = "App For E2E"
    static appName

    def setupSpec() {
        testCount = 0
        def username = "e2e_test_user"
        def password = "e2e_password"
        to LoginPage
        loginModule.login(username,password)
        at MenuPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "Go To Asset Applications"() {
        testKey = "TM-XXXX"
        given:
        at MenuPage
        when:
        menuModule.goToApplications()
        then:
        at ApplicationListPage
    }

    def "Filter Applications on List and get first occurrence"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = filterPattern
        waitFor{alFirstAppName.text().trim().contains(filterPattern)}
        appName = alFirstAppName.text().trim()
        waitFor{alFirstAppName.click()}
        then:
        at ApplicationDetailsPage
// TODO Following item fetch by data-content cannot be located as self (Label and Value have the same properties)
        adModalAppName[1].text().trim() == appName
    }

    def "Open Edit Application Modal Window by Edit Button"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationDetailsPage
        when:
        waitFor {adModalEditBtn.click()}
        then:
        at ApplicationEditionPage
        waitFor {aeModalAppName.value() ==  appName}
        when:
        waitFor {aeModalCancelBtn.click()}
        then:
        at ApplicationListPage
    }

    def "Filter Applications on List again using the app name"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = appName
        waitFor{alFirstAppName.text().trim() == appName}
        then:
        at ApplicationListPage
    }

    def "Open Edit Application Modal Window By Edit Icon"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationListPage
        when:
        waitFor {alFirstAppEdit.click()}
        then:
        at ApplicationEditionPage
        aeModalAppName.value() ==  appName
    }

    def "Delete Application"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationEditionPage
        when:
        withConfirm(true){waitFor {aeModalDeleteBtn.click() }}
        then:
        at ApplicationListPage
    }

    def "Validate Application is not on List"() {
        testKey = "TM-XXXX"
        given:
        at ApplicationListPage
        when:
        waitFor {alNameFilter.click()}
        alNameFilter = appName
        then:
        at ApplicationListPage
        waitFor{alGridRows.size() == 0}
    }


}

