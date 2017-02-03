package net.transitionmanager.service

import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.commons.GrailsApplication
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

/**
 * Methods to interact with Camel components
 */
@Slf4j(value='logger')
// class CamelService implements ServiceMethods, InitializingBean {
class RoutingService implements InitializingBean {

	AuditService auditService
	AwsService awsService
	// TaskService taskService

	GrailsApplication grailsApplication

	static transactional=false
	static Map routeTable = [:]

	void afterPropertiesSet() {
		def config = grailsApplication.config

		// Build up the routing table of method services
		routeTable << [ updateTransportStatus: [service: awsService, method: 'updateTransportStatus']]
	}

	/**
	 * Used to send a message object to the SNS service
	 * @param topicName - the name of the queue configured in AWS SNS
	 * @param message - an object to be sent with the message. This will be a JSON payload.
	 */
	void processMessage(Object message) {
		String method
		if (! (message instanceof String) ) {
			log.error "processMessage() called with invalid message (type=${message?.getClass().getName()}, value=$message)"
		} else {
			if (! message ) {
				log.error "processMessage() called with empty message"
			} else {
				JsonSlurper slurper = new groovy.json.JsonSlurper()
				Map map
				try {
	 				map = slurper.parseText(message)
	 			} catch(e) {
	 				log.error "processMessage() message was not propertly formed JSON (value=$message)"
	 			}

	 			if (map) {
					if (! map.containsKey('method')) {
						log.error "processMessage() called with missing method (value=$message)"
					} else {
						method = map.method
						if (! routeTable.containsKey(method)) {
							log.error "processMessage() called with invalid method (value=$message)"
						} else {
							// Invoke the method through some groovy awesomeness...
							log.debug "processMessage() attempting to invoke $message"
							routeTable[method].service."${routeTable[method]['method']}"(map)
						}
					}
				}
			}
		}
	}

}