import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.commons.lang.RandomStringUtils as RSU
import test.helper.ProviderTestHelper

class ApiActionTestHelper {
    /**
     * Create an ApiAction with some default data
     * @param project
     * @return
     */
    ApiAction createApiAction(Project project, Provider provider = null ) {
        if (!provider) {
            ProviderTestHelper providerHelper = new ProviderTestHelper()
            provider = providerHelper.createProvider(project)
        }

        ApiAction apiAction = new ApiAction(
                name: RSU.randomAlphabetic(10),
                provider: provider,
                description: 'This is a bogus action for testing',
                agentClass: AgentClass.HTTP,
                agentMethod: 'sendSnsNotification',
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
        apiAction.save(flush:true, failOnError: true)
        return apiAction
    }
}
