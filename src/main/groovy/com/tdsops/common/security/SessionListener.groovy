package com.tdsops.common.security

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.spring.TdsUserDetails
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.StringUtil
import grails.converters.JSON
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.security.SecurityService

import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionAttributeListener
import javax.servlet.http.HttpSessionBindingEvent
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

@CompileStatic
@Slf4j(value='logger')
class SessionListener implements HttpSessionListener, HttpSessionAttributeListener {

	private static final String LOGGER_PREFIX = 'SESSION_ACTIVITY: '
	private static final int SESSION_VISIBLE_EDGE_SIZE = 5

	@CompileStatic
	static enum EventType {
		sessionCreated,
		sessionDestroyed,
		attributeAdded,
		attributeReplaced,
		attributeRemoved
	}

	void sessionCreated(HttpSessionEvent event) {
		processEvent(type: EventType.sessionCreated, event: event)
	}

	void sessionDestroyed(HttpSessionEvent event) {
		processEvent(type: EventType.sessionDestroyed, event: event)
	}

	void attributeAdded(HttpSessionBindingEvent event) {
		processEvent(type: EventType.attributeAdded, sessionBindingEvent: event)
	}

	void attributeReplaced(HttpSessionBindingEvent event) {
		processEvent(type: EventType.attributeReplaced, sessionBindingEvent: event)
	}

	void attributeRemoved(HttpSessionBindingEvent event) {
		processEvent(type: EventType.attributeRemoved, sessionBindingEvent: event)
	}

	private void processEvent(Map data) {
		try {
			if (!debugEnabled) return

			HttpSessionEvent event = (HttpSessionEvent) data.event
			HttpSessionBindingEvent bindingEvent = (HttpSessionBindingEvent) data.sessionBindingEvent
			if (bindingEvent) {
				event = bindingEvent
			}

			EventType type = (EventType) data.type

			Map<String, Object> jsonData = [:]
			jsonData.eventTime = new Date()
			jsonData.type = type.name()
			HttpSession session = event.session
			jsonData.sessionId = StringUtil.maskStringCenter(session.id, SESSION_VISIBLE_EDGE_SIZE)

			if (bindingEvent) {
				jsonData.attributeName = bindingEvent.name
				if (type != EventType.attributeRemoved) {
					jsonData.attributeValueType = bindingEvent.value?.getClass()?.name
					String valueToString
					try {
						valueToString = bindingEvent.value == null ? '{{ null }}' : bindingEvent.value.toString()
					}
					catch (e) {
						valueToString = '{{ Error occurred rendering as String }}'
					}

					if (valueToString.length() > 500) {
						valueToString = valueToString.substring(0, 500) + '...'
					}
					jsonData.attributeValue = valueToString
				}
			}

			/*
			// TODO: TM-13134: Commented should we remove it? right now we can enable it and it will log in Debug node only
			if (
				logger.debugEnabled &&
						type in [EventType.sessionCreated, EventType.sessionDestroyed]
			) {
				logger.debug """
					***********
					*********** $type ${jsonData.sessionId}
					***********
				""".stripIndent()
				try {
					// Create a stacktrace to puposefully log where in the application that the session was created
					throw new RuntimeException("$type")

				} catch (e) {
					logger.debug ExceptionUtil.stackTraceToString(e, 80)
				}
			}
			*/

			try {
				if (type == EventType.sessionCreated) {
					jsonData.sessionCreationTime = session.creationTime
				}
				jsonData.sessionLastAccessedTime = session.lastAccessedTime
			} catch (IllegalStateException e) {
				// ignored; both method calls throw IllegalStateException if invalidated,
				// but there's no way to check that the session is still active
			}

			String ipAddress = HtmlUtil.getRemoteIp()
			if (ipAddress == 'Unknown') ipAddress = securityService.ipAddress
			jsonData.ip = ipAddress
			jsonData.authInfo = authInfo

			logger.debug '{} {}', LOGGER_PREFIX, (jsonData as JSON)
		} catch (e) {
			handleException 'sessionCreated', e
		}
	}

	private Map<String, Object> getAuthInfo() {
		if (securityService.loggedIn) {
			TdsUserDetails details = securityService.currentUserDetails
			[username: details.username, 'UserLogin.id': details.id,
			 'Person.id': details.personId, roles: details.authorities*.authority]
		}
		else {
			Collections.emptyMap()
		}
	}

	private void handleException(String type, Throwable t) {
		try {
			logger.error t.message, t
		}
		catch (Throwable t2) {
			t2.printStackTrace()
			t?.printStackTrace()
		}
	}

	private boolean isDebugEnabled() {
		logger.debugEnabled
	}

	private SecurityService getSecurityService() {
		ApplicationContextHolder.getBean('securityService', SecurityService)
	}
}
