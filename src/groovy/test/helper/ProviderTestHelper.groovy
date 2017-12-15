package test.helper

import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider

/**
 * Use this class to create provider domain objects in your tests
 */
class ProviderTestHelper {

    /**
     * Create a default provider domain object
     * @param project
     * @return
     */
    Provider createProvider(Project project) {
        Provider provider = new Provider(
                name: 'Test provider',
                comment: 'Test comment',
                description: 'Test description',
                project: project
        ).save(flush: true, failOnError: true)
        return provider
    }

}
