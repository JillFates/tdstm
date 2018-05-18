package net.transitionmanager.agent

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.util.logging.Slf4j
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.service.HttpProducerService
import groovy.transform.CompileStatic

/**
 * Methods to interact with the HTTP services
 */
@Slf4j()
@Singleton(strict=false)
@CompileStatic
class HttpAgent extends AbstractAgent {

    HttpProducerService httpProducerService

    HttpAgent() {
        setInfo(AgentClass.HTTP, 'HTTP API')

        setDictionary( [
            callEndpoint: new DictionaryItem( [
                agentMethod: 'callEndpoint',
				name: 'Call Endpoint',
				description: 'Performs a call to an HTTP endpoint',
				endpointUrl: 'https://SOME-DOMAIN/SOME/PATH',
				docUrl: '',
				method: 'invokeHttpRequest',
				producesData: 0,
				params: []
            ])
        ] )

        httpProducerService = (HttpProducerService) ApplicationContextHolder.getBean('httpProducerService')
    }

    /**
     * Call the service supporting this agent execute method.
     * @param actionRequest
     * @return
     */
    ApiActionResponse invokeHttpRequest(ActionRequest actionRequest) {
        return httpProducerService.executeCall(actionRequest)
    }
}
