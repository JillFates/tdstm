package net.transitionmanager.agent

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.util.logging.Slf4j
import net.transitionmanager.service.ServiceNowService

/**
 * Methods to interact with ServiceNow fetch/download assets lists
 */
@Slf4j
@Singleton(strict=false)
class ServiceNowAgent extends AbstractAgent {

    public ServiceNowService serviceNowService

    /*
     * Constructor
     */
    ServiceNowAgent() {
        setInfo(AgentClass.SERVICE_NOW, 'ServiceNow API')

        setDictionary( [
                fetchAssets: new DictionaryItem([
                        name: 'fetchAssets',
                        description: 'Fetch assets from ServiceNow',
                        method: 'fetchAssets',
                        params: [:],
                        results: invokeResults()
                ])
        ].asImmutable() )

        serviceNowService = ApplicationContextHolder.getBean('serviceNowService')
    }

    /**
     * Used to fetch/download assets lists from ServiceNow
     * @param payload
     * @return
     */
    Map fetchAssets(Object payload) {
        Map result = serviceNowService.fetchAssets(payload)
        log.debug 'Result of fetch assets. {}', result

        return result
    }

}
