package com.tdsops.common.builder

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialEnvironment
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.UrlUtil
import net.transitionmanager.domain.Credential
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.service.CredentialService
import net.transitionmanager.service.InvalidRequestException
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition
import org.apache.http.client.utils.URIBuilder

/**
 * This bean is used to create a Camel RouteDefinition used by
 * RestfulProducerService during API Action invocation
 *
 * @see <code>resources.groovy</code> for definition
 */
class RestfulRouteBuilder extends RouteBuilder {
    private static final String ROUTE_ID_PREFIX = 'TM_CAMEL_ROUTE_'

    CredentialService credentialService

    @Override
    void configure() throws Exception {
        // Do nothing here since we are creating routes dynamically
    }

    /**
     * Builds a Camel route definition with the provided action request
     * @param actionRequest - the action request
     * @return
     */
    RouteDefinition getRouteDefinition(ActionRequest actionRequest) {

        String apiActionId = actionRequest.param.actionId
        String taskId = actionRequest.param.taskId
        String routeId = getRouteId(apiActionId)

        RouteDefinition routeDefinition = from("direct:RESTfulCall-" + apiActionId)
        routeDefinition = routeDefinition.setProperty("API_ACTION_ID", constant(apiActionId))
        routeDefinition = routeDefinition.setProperty("TASK_ID", constant(taskId))
        routeDefinition = routeDefinition.setProperty("ROUTE_ID", constant(routeId))
        routeDefinition = routeDefinition.setProperty("API_ACTION_CONTEXT", constant(JsonUtil.toJson(actionRequest.param)))

        routeDefinition = routeDefinition.setBody(constant(null))

        if (actionRequest.config.hasProperty(Exchange.HTTP_METHOD)) {
            routeDefinition.setHeader(Exchange.HTTP_METHOD, constant(actionRequest.config.getProperty(Exchange.HTTP_METHOD)))
        }

        if (actionRequest.config.hasProperty(Exchange.ACCEPT_CONTENT_TYPE)) {
            routeDefinition.setHeader(Exchange.ACCEPT_CONTENT_TYPE, constant(actionRequest.config.getProperty(Exchange.ACCEPT_CONTENT_TYPE)))
        }

        if (actionRequest.config.hasProperty(Exchange.CONTENT_TYPE)) {
            routeDefinition.setHeader(Exchange.CONTENT_TYPE, constant(actionRequest.config.getProperty(Exchange.CONTENT_TYPE)))
        }

        routeDefinition = routeDefinition.to(UrlUtil.sanitizeUrlForCamel(buildUrl(routeDefinition, actionRequest)))
        routeDefinition = routeDefinition.to(buildRESTfulReactionEndpoint(actionRequest.param))
        routeDefinition = routeDefinition.routeId(routeId)
        routeDefinition = routeDefinition.stop()
        return routeDefinition
    }

    /**
     * Builds a Camel route id
     * @param apiActionId
     * @return
     */
    private String getRouteId(String apiActionId) {
        return ROUTE_ID_PREFIX + apiActionId
    }

    /**
     * It uses "reaction" key/value from ActionRequest parameters to determine the reaction method to call after
     * calling the endpoint.
     * @param payload
     * @return
     */
    private String buildRESTfulReactionEndpoint(Object payload) {
        StringBuilder restfulEndpoint = new StringBuilder()
        if (payload.callbackMethod) {
            restfulEndpoint.append("bean:restfulProducerService?method=").append(payload.callbackMethod)
        } else {
            restfulEndpoint.append("bean:restfulProducerService?method=reaction")
        }
    }

    /**
     * Builds the end point URL including required and  authentication parameters when provided.
     * @param actionRequest
     * @return
     */
    private String buildUrl(RouteDefinition routeDefinition, ActionRequest actionRequest) {
		String endpointUrl = actionRequest.config.getProperty(Exchange.HTTP_URL)
        URIBuilder builder = new URIBuilder(endpointUrl)
        builder.setPath(actionRequest.config.getProperty(Exchange.HTTP_PATH))
        // do not throw HttpOperationFailedException and instead return control to action invocation flow to eval result
        // see http://camel.apache.org/http4.html#HttpEndpoint Options
        builder.addParameter('throwExceptionOnFailure', 'false')

		// only provides a trust store if endpoint url is secure and credentials are nor for production
		if (UrlUtil.isSecure(endpointUrl)) {
			if (actionRequest.param.credentials && actionRequest.param.credentials.environment != CredentialEnvironment.PRODUCTION.name()) {
				// for more details see CustomHttpClientConfigurer class
				builder.addParameter('httpClientConfigurer', 'customHttpClientConfigurer')
			}
		}

        if (actionRequest.param.credentials) {
			if (actionRequest.param.credentials.status == CredentialStatus.INACTIVE.name()) {
				throw new InvalidRequestException("The Credential associated with API Action is disabled")
			}

			// fetch a fresh copy of the credentials to have access to password and salt when needed
			// TODO use CredentialService if possible
			Credential credential = Credential.read(actionRequest.param.credentials.id)
			switch (credential.authenticationMethod) {
				case AuthenticationMethod.BASIC_AUTH:
					builder.addParameter('authUsername', credential.username)
					builder.addParameter( 'authPassword', credentialService.decryptPassword(credential))
					break;
				case AuthenticationMethod.COOKIE:
					Map<String, ?> authentication = credentialService.authenticate(credential)
					routeDefinition.setHeader(authentication.sessionName, constant(authentication.sessionValue))
					break;
				default:
					throw new RuntimeException("Authentication method ${credential.authenticationMethod} has not been implemented in RestfulRouteBuilder")
			}
        }

        URL url = builder.build().toURL()
        return url.toString()
    }
}