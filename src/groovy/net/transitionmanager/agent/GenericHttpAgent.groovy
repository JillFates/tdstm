package net.transitionmanager.agent

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.service.HttpProducerService

/**
 * This class represents a generic Http Agent for Api Actions that perform actions
 * using the HttpProducerService.
 *
 * Keep this agent in order to re-use all abstract agent methods wired along with ApiAction invocation.
 */
@Slf4j
@CompileStatic
class GenericHttpAgent extends AbstractAgent {
	HttpProducerService httpProducerService

	GenericHttpAgent() {
		setInfo(null, 'Generic Http Agent')
		httpProducerService = (HttpProducerService) ApplicationContextHolder.getBean('httpProducerService')
	}

	/**
	 * Call the service supporting this agent execute method. This method name <code>invokeHttpRequest</code> is referenced
	 * by HttpAgent and VMwarevCenterAgent dictionary in the "method" property of every dictionary item.
	 *
	 * @param actionRequest - the api action request
	 * @return an api action response
	 */
	ApiActionResponse invokeHttpRequest(ActionRequest actionRequest) {
		return httpProducerService.executeCall(actionRequest)
	}

	/**
	 * Used to fetch/download assets lists from ServiceNow. This method name <code>fetchAssetList</code> is referenced
	 * by ServiceNow dictionary in the "method" property of every dictionary item.
	 *
	 * @param actionRequest - the api action request
	 * @return an api action response
	 */
	ApiActionResponse fetchAssetList(ActionRequest actionRequest) {
		return httpProducerService.executeCall(actionRequest)
	}

}
