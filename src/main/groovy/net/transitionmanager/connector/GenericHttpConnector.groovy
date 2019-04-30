package net.transitionmanager.connector

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.action.HttpProducerService

/**
 * This class represents a generic Http Connector for Api Actions that perform actions
 * using the HttpProducerService.
 *
 * Keep this connector in order to re-use all abstract connector methods wired along with ApiAction invocation.
 */
@Slf4j
@CompileStatic
class GenericHttpConnector extends AbstractConnector {
	HttpProducerService httpProducerService

	GenericHttpConnector() {
		setInfo('Generic Http Connector')
		httpProducerService = (HttpProducerService) ApplicationContextHolder.getBean('httpProducerService')
	}

	/**
	 * Call the service supporting this connector execute method. This method name <code>invokeHttpRequest</code> is referenced
	 * by HttpConnector and VMwarevCenterConnector dictionary in the "method" property of every dictionary item.
	 *
	 * @param actionRequest - the api action request
	 * @return an api action response
	 */
	ApiActionResponse invokeHttpRequest(ActionRequest actionRequest) {
		return httpProducerService.executeCall(actionRequest)
	}

}
