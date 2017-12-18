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
    Provider createProvider(Project project) {
        Provider provider = new Provider(
                name: RSU.randomAlphabetic(10),
                comment: 'Test comment',
                description: 'Test description',
                project: project
        ).save(flush: true, failOnError: true)
        return provider
    }

}
