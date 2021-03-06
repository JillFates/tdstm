package test.helper

import grails.gorm.transactions.Transactional
import net.transitionmanager.action.Provider
import net.transitionmanager.project.Project
import org.apache.commons.lang3.RandomStringUtils as RSU
/**
 * Use this class to create provider domain objects in your tests
 */
@Transactional
class ProviderTestHelper {

    /**
     * Create a default provider domain object
     * @param project
     * @return
     */
    Provider createProvider(Project project, String name = null) {
        Provider provider = new Provider(
                name: name ?: RSU.randomAlphabetic(10),
                comment: 'Test comment',
                description: 'Test description',
                project: project
        ).save(flush: true)
        return provider
    }

    /**
     * Create a provider if not exists from given name for E2EProjectSpec to persist at server DB
     * @param: name
     * @param: project
     * @param: event
     * @param: useForPlanning defaulted true
     * @returm the provider
     */
    Provider createProviderMap(Project project, Map providerData) {
        Provider existingProvider = Provider.findWhere([name: providerData.name, project: project])
        if (!existingProvider) {
            Provider provider = new Provider(
                    name: providerData.name,
                    comment: providerData.comment ? providerData.comment :'Test comment',
                    description: providerData.description ? providerData.description : 'Test description',
                    project: project
            ).save(flush: true)
            return provider
        }
        return existingProvider
    }

}
