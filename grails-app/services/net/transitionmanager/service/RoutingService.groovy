package net.transitionmanager.service

import net.transitionmanager.domain.Person
import net.transitionmanager.service.InvalidRequestException
import com.tdsops.common.lang.ExceptionUtil
import org.apache.camel.Exchange
import org.apache.camel.Message
import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.commons.GrailsApplication
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

/**
 * The purpose of the RoutingService is to handle inbound route requests with a JSON message
 * and determine the target method. If defined then it will route the message to the appropriate
 * service.
 */
@Slf4j(value='logger')
class RoutingService implements InitializingBean {

	AuditService auditService
	AwsService awsService
	TaskService taskService

	GrailsApplication grailsApplication

	static transactional=false
	static Map routeTable = [:]

	void afterPropertiesSet() {
		def config = grailsApplication.config

		// Build up the routing table of method services
		routeTable = [
			addTaskComment: [service: taskService, method: 'addTaskCommentByMessage'],
			updateTaskState: [service: taskService, method: 'updateTaskStateByMessage']
		]
	}

	/**
	 * Used to process aggregated messages coming from SQS Service
	 * @param exchangeList
	 *
	 * @see net.transitionmanager.routes.AwsSqsRoute#configure()
	 */
	void processMessages(List<String> exchangeList) {
		if (exchangeList) {
			logger.debug 'Received {} messages, attempting to proccess them.', exchangeList.size()
			for (String exchange : exchangeList) {
				processMessage(exchange)
			}
		}
	}

	/**
	 * Used to process messages coming from SQS Service
	 * @param topicName - the name of the queue configured in AWS SNS
	 * @param message - an object to be sent with the message. This will be a JSON payload.
	 */
	void processMessage(String message) {
		String method
		if (! (message instanceof String) ) {
			log.warn "processMessage() called with invalid message (type=${message?.getClass().getName()}, value=$message)"
		} else {
			if (! message ) {
				log.warn "processMessage() called with empty message"
			} else {
				JsonSlurper slurper = new groovy.json.JsonSlurper()
				Map map
				try {
	 				map = slurper.parseText(message)
	 			} catch(e) {
	 				log.warn "processMessage() message was not propertly formed JSON (value=$message)"
	 			}

	 			if (map) {
	 				boolean isCallback = map.containsKey('callbackMethod')
	 				method = map.method ?: map.callbackMethod
					if (! method) {
						log.warn "processMessage() called with missing method (value=$message)"
					} else {
						// method = map.method
						if (! routeTable.containsKey(method)) {
							log.warn "processMessage() called with invalid method (value=$message)"
						} else {
							try {
								if (isCallback) {
									// Needed to put in a sleep for 4 sec for callbacks so that the
									// original thread finishes and commits the status change.
									sleep(4000)
								}

								// Invoke the method through some groovy awesomeness...
								log.debug "processMessage() attempting to invoke $message"
								routeTable[method].service."${routeTable[method]['method']}"(map)

							} catch (InvalidRequestException e) {
								log.warn "processMessage() called with invalid request (${e.getMessage()}) : $map"
							} catch (e) {
								log.error ExceptionUtil.stackTraceToString('processMessage() encounted runtime error :', e)
							}
						}
					}
				}
			}
		}
	}

}