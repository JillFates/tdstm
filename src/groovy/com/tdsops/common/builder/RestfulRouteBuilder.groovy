package com.tdsops.common.builder

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdssrc.grails.JsonUtil
import net.transitionmanager.integration.ActionRequest
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
            routeDefinition.setHeader(Exchange.HTTP_METHOD, constant(actionRequest.config.getProperty(Exchange.CONTENT_TYPE)))
        }

        if (actionRequest.config.hasProperty(Exchange.CONTENT_TYPE)) {
            routeDefinition.setHeader(Exchange.HTTP_METHOD, constant(actionRequest.config.getProperty(Exchange.ACCEPT_CONTENT_TYPE)))
        }
        routeDefinition = routeDefinition.to(sanitizeHostname(buildUrl(actionRequest)))
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
     * Replaces HTTP, HTTPS protocols from the hostname by Camel HTTP4
     * @param hostname
     * @return
     */
    private String sanitizeHostname(String hostname) {
        // do this for http4 camel component
        return "http4:" + hostname.replaceAll("(https:|http:)", "")
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
    private String buildUrl(ActionRequest actionRequest) {
        URIBuilder builder = new URIBuilder(actionRequest.config.getProperty(Exchange.HTTP_URL))
        builder.setPath(actionRequest.config.getProperty(Exchange.HTTP_PATH))
        // do not throw HttpOperationFailedException and instead return control to action invocation flow to eval result
        // see http://camel.apache.org/http4.html#HttpEndpoint Options
        builder.addParameter('throwExceptionOnFailure', 'false')

        if (actionRequest.param.credentials) {
            Map<String, ?> credentials = actionRequest.param.credentials
            if (credentials.status == CredentialStatus.ACTIVE.name()) {
                // if basic auth
                if (credentials.method == AuthenticationMethod.HTTP_BASIC.name()) {
                    builder.addParameter('authUsername', credentials.accessKey)
                    builder.addParameter('authPassword', credentials.password)
                }
            }
        }

        URL url = builder.build().toURL()
        return url.toString()
    }
}
