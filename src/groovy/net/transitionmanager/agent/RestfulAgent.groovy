package net.transitionmanager.agent

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.util.logging.Slf4j
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.service.RestfulProducerService

/**
 * Methods to interact with the RESTful/HTTP services
 */
@Slf4j
@Singleton(strict=false)
class RestfulAgent extends AbstractAgent {
    public RestfulProducerService restfulProducerService

    RestfulAgent() {
        setInfo(AgentClass.RESTFULL, 'RESTful API')

        setDictionary( [
                executeCall: new DictionaryItem([
                        name: 'executeCall',
                        description: 'Fetch assets from ServiceNow',
                        method: 'executeCall',
                        params: restfulParams(),
                        results: invokeResults()
                ])
        ].asImmutable() )

        restfulProducerService = ApplicationContextHolder.getBean('restfulProducerService')
    }

    /**
     * Call the service supporting this agent execute method.
     * @param actionRequest
     * @return
     */
    void executeCall(ActionRequest actionRequest) {
        restfulProducerService.executeCall(actionRequest)
    }
}
