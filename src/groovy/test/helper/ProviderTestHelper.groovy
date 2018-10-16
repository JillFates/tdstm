package test.helper

import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider

import org.apache.commons.lang.RandomStringUtils as RSU

/**
 * Use this class to create provider domain objects in your tests
 */
class ProviderTestHelper {

    /**
     * Create a default provider domain object
     * @param project
     * @return
     */
    Provider createProvider(Project project, Map providerData = null) {
        Provider existingProvider
        // Because E2E project use to insert or update at database and BE integrations just rollback
        // first from E2E we need to check if it exists to avoid a broken integration test on creation
        if (providerData) {
            existingProvider = Provider.findWhere([name: providerData.name, project: project])
            if (existingProvider) {
                return existingProvider
            }
        }

        Provider provider = new Provider(
                name: providerData.name ? providerData.name : RSU.randomAlphabetic(10),
                comment: providerData.comment ? providerData.comment :'Test comment',
                description: providerData.description ? providerData.description : 'Test description',
                project: project
        ).save(flush: true, failOnError: true)
        return provider
    }

}
