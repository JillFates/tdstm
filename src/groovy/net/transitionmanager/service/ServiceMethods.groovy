package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.ObjectError
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

trait ServiceMethods {

	GrailsApplication grailsApplication
	MessageSourceService messageSourceService

	/**
	 * Calls get() to retrieve a domain class instance by id. The provided id can
	 * be the correct type (e.g. Long) or a String/GString (CharSequence) since
	 * Grails will convert that, and if the id is actually an instance of the
	 * class it is returned instead (to allow for methods that accept with an
	 * instance or an id as an argument).
	 *
	 * @param type  the class
	 * @param id  the id in number or string form, or a domain class instance
	 * @param throwException  if true, throw exceptions if the id is invalid or no
	 * instance is found, otherwise return null
	 * @return  the instance
	 */
	def <T> T get(Class<T> type, id, boolean throwException = true) {
		if (!id) return null

		if (id in type) {
			return (T) id
		}

		if (id instanceof CharSequence) {
			if (GormUtil.hasStringId(type)) {
				return doGet(type, id, throwException)
			}

			try {
				return doGet(type, id.toLong(), throwException)
			}
			catch (NumberFormatException e) {
				throw new InvalidParamException('Unable to retrieve ' + type.simpleName + ' with invalid id: ' + id)
			}
		}

		if (id instanceof Number) {
			return doGet(type, ((Number)id).longValue(), throwException)
		}

		throw new InvalidParamException("Unable to retrieve an instance of $type.name with unsupported id type $id (${id?.getClass()?.name})")
	}

	private <T> T doGet(Class<T> type, id, boolean throwException) {
		T t
		if (id instanceof Long && id == 0L) {
			t = null
		}
		else {
			t = (T) type.get(id)
		}

		if (!t && throwException) {
			// TODO
		}

		t
	}

	def <T> T save(T instance, boolean flush = false) {
		if (instance == null) return null

		try {
			instance.save(flush: flush)

			if (instance.hasErrors()) {
				log.error("save() Validation errors saving ${instance.getClass().simpleName} with id ${instance.id} ${GormUtil.allErrorsString(instance)}")
			}
		}
		catch (e) {
			// TODO : JPM 12/2016 - the save() method buries exceptions which is WRONG
			log.error(e.message, e)
		}

		instance
	}

	/**
	 * Used to gain access to the HttpSession request object when there is an HTTP Request.
	 * For calls when there is no HTTP Session (e.g. Quartz jobs) a null value will be returned.
	 * @return the Http Request session object
	 */
	HttpSession getSession() {
		HttpSession session = null
		if (RequestContextHolder.getRequestAttributes()) {
			GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
			if (grailsWebRequest) {
				HttpServletRequest request = grailsWebRequest.currentRequest
				session = request.session
			}
		}
		return session
	}

	/**
	 * Get an i18n message
	 * @param code - message code
	 * @return
	 */
	// TODO : JPM 2/2018 : Shouldn't this take the Locale as a default?
	String i18nMessage(String code) {
		return i18nMessage(code, [] as Object[], '')
	}

	/**
	 * Get an i18n message
	 * @param code - message code
	 * @param defaultMessage - default message if message code is not found
	 * @return
	 */
	// TODO : JPM 2/2018 : Shouldn't this take the Locale as a default?
	String i18nMessage(String code, String defaultMessage) {
		return i18nMessage(code, [] as Object[], defaultMessage)
	}

	/**
	 * Return an error message based on the ObjectError and Locale provided.
	 * @param objectError
	 * @param locale
	 * @return
	 */
	String i18nMessage(ObjectError objectError, Locale locale = LocaleContextHolder.locale) {
		return messageSourceService.i18nMessage(objectError, locale)
	}

	/**
	 * Get an i18n message
	 * @param code - message code
	 * @param args - message arguments to interpolate, e.g. `{0}` marks
	 * @return
	 */
	String i18nMessage(String code, Object[] args) {
		return i18nMessage(code, args, null)
	}

	/**
	 * Get an i18n message
	 * @param code - message code
	 * @param args - message arguments to interpolate, e.g. `{0}` marks
	 * @param defaultMessage - default message if message code is not found
	 * @param locale - message locale, ENGLISH, FRENCH, US, UK
	 * @return
	 */
	String i18nMessage(String code, Object[] args, String defaultMessage, Locale locale = LocaleContextHolder.locale) {
		return messageSourceService.i18nMessage(code, args, defaultMessage, locale)
	}

	/**
	 * Used to throw an exception with a i18n message
	 * @param code - message code
	 * @param args - message arguments to interpolate, e.g. `{0}` marks
	 * @param defaultMessage - default message if message code is not found
	 * @param locale - message locale, ENGLISH, FRENCH, US, UK (optional)
	 */	
	void throwException(Class exception, String messageCode, String defaultMessage, Locale locale = LocaleContextHolder.locale) {
		throwException(exception, messageCode, [] as Object[], defaultMessage, locale)
	}

	/**
	 * Used to throw an exception with a i18n message
	 * @param code - message code
	 * @param args - message arguments to interpolate, e.g. `{0}` marks
	 * @param defaultMessage - default message if message code is not found
	 * @param locale - message locale, ENGLISH, FRENCH, US, UK (optional)
	 */	
	void throwException(Class exception, String messageCode, List args, String defaultMessage, Locale locale = LocaleContextHolder.locale) {
		String i18nMsg = i18nMessage(messageCode, args as Object[], defaultMessage, locale)
		Exception ex = exception.newInstance(i18nMsg)
		throw ex
	}

}
