import net.transitionmanager.connector.CallbackMode
import net.transitionmanager.action.ApiAction
import net.transitionmanager.action.ApiCatalog
import net.transitionmanager.project.Project
import net.transitionmanager.action.Provider
import org.apache.commons.lang3.RandomStringUtils as RSU
import test.helper.ProviderTestHelper

class ApiActionTestHelper {
    /**
     * Create an ApiAction with some default data
     * @param project
     * @return
     */
    ApiAction createApiAction(Project project, Provider provider = null, ApiCatalog apiCatalog = null) {
        if (!provider) {
            ProviderTestHelper providerHelper = new ProviderTestHelper()
            provider = providerHelper.createProvider(project)
        }

        ApiAction apiAction = new ApiAction(
                name: RSU.randomAlphabetic(10),
                provider: provider,
                description: 'This is a bogus action for testing',
                apiCatalog: apiCatalog,
                connectorMethod: 'sendSnsNotification',
                methodParams: null,
                asyncQueue: 'test_outbound_queue',
                callbackMethod: 'updateTaskState',
                callbackMode: CallbackMode.MESSAGE,
                pollingFrequency: 0,
                pollingLapsedAfter: 0,
                pollingStalledAfter: 0,
                project: project,
                reactionScripts: "{\"SUCCESS\": \"success\",\"STATUS\": \"status\",\"ERROR\": \"error\"}",
                useWithAsset: 0,
                useWithTask: 0
        )
        apiAction.save(flush:true)
        return apiAction
    }
}
