package specs.Projects.Providers

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Projects.Providers.CreateProviderPage
import pages.Projects.Providers.ProvidersPage
import pages.Projects.Providers.ProvidersDetailPage
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage

import geb.driver.CachingDriverFactory

/**
 * This Spec cleans up the providers list.
 * If there are more than 11 providers (all we really need) then it will delete 8 of them (if possible).
 * @author Alvaro Navarro
 */


@Stepwise
class ProviderCleanUpSpec extends GebReportingSpec{

    def testKey
    static testCount
    static provName = "E2E Provider"

    def setupSpec() {
        CachingDriverFactory.clearCacheAndQuitDriver()
        
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        projectsModule.goToProviders()
        at ProvidersPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Provider Cleanup is done"() {
        when: 'The User is on the Providers Page'
            at ProvidersPage
        and: 'Filters the Providers by the E2E-created Providers'
            filterByName provName
        and: 'The cleanup of providers is done' //A max of 8 providers are deleted by run
            cleanUpProviders()
        then: 'The cleanup is done and we remain on the Providers page'
            at ProvidersPage
    }


}
