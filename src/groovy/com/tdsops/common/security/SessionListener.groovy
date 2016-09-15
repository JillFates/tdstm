package com.tdsops.common.security

import com.tdssrc.grails.HtmlUtil

import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionAttributeListener
import javax.servlet.http.HttpSessionBindingEvent
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

import org.apache.log4j.Logger
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.support.DefaultSubjectContext

import com.tdssrc.grails.HtmlUtil

class SessionListener implements HttpSessionListener, HttpSessionAttributeListener {

	private static final String SHIRO_KEY = DefaultSubjectContext.PRINCIPALS_SESSION_KEY
	private static final String LOGIN_PERSON = 'LOGIN_PERSON'
	private static final String LOGGER_PREFIX = 'SESSION_ACTIVITY: '

	private final Logger log = Logger.getLogger(getClass())

	void sessionCreated(HttpSessionEvent event) {
		log.effectiveLevel
		try {
			if (!log.debugEnabled) return

			HttpSession session = event.session

			log.debug LOGGER_PREFIX + 'Created; id: ' + session.id + ', at ' + session.creationTime +
				', ip: ' + HtmlUtil.getRemoteIp()
		}
		catch (ignored) {}
	}

	void sessionDestroyed(HttpSessionEvent event) {
		try {
			if (!log.debugEnabled) return

			HttpSession session = event.session

			String ipAddr = HtmlUtil.getRemoteIp()

			StringBuilder sb = new StringBuilder(LOGGER_PREFIX + 'Destroyed; ')
			sb << 'id:' << session.id << ' @ ' << ipAddr << ' created at ' << session.creationTime
			sb << ' (age: ' << (System.currentTimeMillis() - session.creationTime) << '), '
			sb << 'lastAccessed at ' << session.lastAccessedTime
			sb << ' (' + (System.currentTimeMillis() - session.lastAccessedTime) + ' ms ago), '

			for (Enumeration<String> names = session.attributeNames; names.hasMoreElements(); ) {
				String name = names.nextElement()
				if (name == SHIRO_KEY || name == LOGIN_PERSON) {
					// boolean added = false
					if (name == SHIRO_KEY) {
						PrincipalCollection principals = session.getAttribute(name)
						sb << 'Shiro principal found in session: ' << principals.primaryPrincipal
						// added = true
					}

					sb << ', ip: ' + HTMLUtil.getRemoteIp()
					/*
					if (name == LOGIN_PERSON) {
						if (added) sb << ', '
						sb << 'LOGIN_PERSON found in session: ' + session[LOGIN_PERSON]
					}
					*/
					break
				}
			}

			/*
			sb << 'Attributes: '

			String delimiter = ''
			for (Enumeration<String> names = session.attributeNames; names.hasMoreElements(); ) {
				String name = names.nextElement()
				def value = session.getAttribute(name)
				sb << delimiter
				sb << 'Name: "' << name << '" '
				sb << 'Value: ' << value
				delimiter = ', '
			}
			*/

			log.debug sb.toString()
		}
		catch (ignored) {}
	}

	void attributeAdded(HttpSessionBindingEvent event) {
		try {
			if (!log.debugEnabled) return
			findPrincipal event, 'added to'
		}
		catch (ignored) {}
	}

	void attributeRemoved(HttpSessionBindingEvent event) {
		try {
			if (!log.debugEnabled) return
			findPrincipal event, 'removed from'
		}
		catch (ignored) {}
	}

	void attributeReplaced(HttpSessionBindingEvent event) {
		try {
			if (!log.debugEnabled) return
			findPrincipal event, 'replaced in'
		}
		catch (ignored) {}
	}

	private findPrincipal(HttpSessionBindingEvent event, String what) {
		String name = event.name
		if (name != SHIRO_KEY && name != LOGIN_PERSON) {
			return
		}

		HttpSession session = event.session
		StringBuilder sb = new StringBuilder(LOGGER_PREFIX + 'Attrib Change; ')

		boolean added = false
		if (name == SHIRO_KEY) {
			PrincipalCollection principals = session.getAttribute(name)
			sb << 'Shiro principal ' + what + ' session ' << session.id << ': ' << principals.primaryPrincipal
			added = true
		}

		//if (event.name == LOGIN_PERSON) {
		//	if (added) sb << ', '
		//	sb << 'LOGIN_PERSON ' + what + ' session ' << session.id << ': ' << session[LOGIN_PERSON]
		//}

		if (added) {
			sb << ', ip: ' + HtmlUtil.getRemoteIp()

			log.debug sb.toString()
		}
	}
}
