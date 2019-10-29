package net.transitionmanager.session

import javax.servlet.http.HttpSession

class SessionContext {
	static final String LAST_PAGE_REQUESTED = 'net.transitionmanager.LAST_PAGE_REQUESTED'

	/**
	 * Used to add / set a variable to the session
	 * @param session
	 * @param key
	 * @param value
	 */
	static void set(HttpSession session, String key, Object value) {
		session.setAttribute(key, value)
	}

	/**
	 * Used to access a variable from the session
	 * @param session
	 * @param key
	 * @return
	 */
	static Object get(HttpSession session, String key) {
		session.getAttribute(key)
	}

	/**
	 * Used to remove a variable from the session
	 * @param session
	 * @param key
	 */
	static void remove(HttpSession session, String key) {
		session.removeAttribute(key)
	}

	/**
	 * Used to set the Last Page requested on the session
	 * @param session
	 * @param key
	 * @param value
	 */
	static void setLastPageRequested(HttpSession session, Object value) {
		set(session, LAST_PAGE_REQUESTED, value)
	}

	/**
	 * Used to retrieve the Last Page requested from the session
	 * @param session
	 * @return
	 */
	static String getLastPageRequested(HttpSession session) {
		get(session, LAST_PAGE_REQUESTED)
	}

}
