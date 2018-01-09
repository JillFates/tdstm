package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import org.springframework.context.MessageSource

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.web.context.request.RequestContextHolder

trait ServiceMethods {

	def grailsApplication
	MessageSource messageSource

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
		HttpSession session
		if (RequestContextHolder.getRequestAttributes()) {
			GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
			if (grailsWebRequest) {
				HttpServletRequest request = grailsWebRequest.currentRequest
				session = request.session
			}
		}
	}
}
