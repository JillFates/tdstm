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
                        params: restfulParams(),
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
        callbackMethod(payload, result)

        return result
    }

    /**
     * Used to execute post actions
     * @param payload
     * @param result
     */
    void callbackMethod(Object payload, Map result) {
        try {
            if (payload['callbackMethod'] && result && result['filename']) {
                serviceNowService."${payload['callbackMethod']}"(result)
            } else {
                log.error('Error fetching assets. {}', result)
            }
        } catch (Exception e) {
            log.error('Application error.', e)
        }
    }

}
