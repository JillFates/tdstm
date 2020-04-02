package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import groovy.transform.CompileStatic
import net.transitionmanager.common.MessageSourceService
import net.transitionmanager.common.Timezone
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.LogicException
import net.transitionmanager.project.Project
import grails.core.GrailsApplication
import net.transitionmanager.security.Permission
import net.transitionmanager.security.SecurityService
import  org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.ObjectError
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

trait ServiceMethods {

	GrailsApplication    grailsApplication
	MessageSourceService messageSourceService
	SecurityService      securityService

	/**
	 * Calls get() to retrieve a domain class instance by id. The provided id can
	 * be the correct type (e.g. Long) or a String/GString (CharSequence) since
	 * Grails will convert that, and if the id is actually an instance of the
	 * class it is returned instead (to allow for methods that accept with an
	 * instance or an id as an argument).
	 *
	 * @param type  the class
	 * @param id  the id in number or string form, or a domain class instance
	 * @param currentProject the current project passed down through the controller
	 * @param throwException  if true, throw exceptions if the id is invalid or no
	 * instance is found, otherwise return null
	 * @return  the instance
	 */
	@CompileStatic
	<T> T get(Class<T> type, Object id, Project currentProject, boolean throwException = true) {
		T t

		if (id == null && throwException){
			throw new InvalidParamException('Unable to retrieve ' + type.simpleName + ' with invalid id: ' + id)
		} else if(id == null && !throwException){
			return null
		}

		if (id in type) {
			return (T) id
		}

		if (id instanceof CharSequence) {
			if (GormUtil.hasStringId(type)) {
				return doGet(type, id, throwException)
			}

			try {
				t = doGet(type, id.toLong(), throwException)
			}
			catch (NumberFormatException e) {
				throw new InvalidParamException('Unable to retrieve ' + type.simpleName + ' with invalid id: ' + id)
			}
		}

		if (id instanceof Number) {
			t = doGet(type, ((Number)id).longValue(), throwException)
		}

		if(t == null) {
			throw new InvalidParamException("Unable to retrieve an instance of $type.name with unsupported id type $id (${id?.getClass()?.name})")
		}

		checkProject(t, currentProject)

		return (T)t
	}

	/**
	 * Checks the domain passed in to see if it has a project field and if it is the same ast the current project.
	 * If the project doesn't match and EmptyResult exception is thrown.
	 *
	 * @param domain The domain to check against the current project.
	 * @param currentProject The current project that should have been, passed down through the controller.
	 */
	void checkProject(domain, Project currentProject) {
		if (GormUtil.isDomainProperty(domain, 'project')) {

			if (!currentProject) {
				throw new EmptyResultException()
			}

			if (currentProject.id != domain.project.id) {
				securityService.reportViolation("attempted to access asset from unrelated project (asset ${domain?.id})")
				throw new EmptyResultException()
			}
		}
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
			throw new EmptyResultException()
		}

		t
	}

	def <T> T save(T instance, boolean flush = false) {
		if (instance == null) return null

		try {
			instance.save(flush: flush, failOnError: false)

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
		throwException(exception, messageCode, [] as List, defaultMessage, locale)
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

	/**
	 * Used to throw an exception in switch/default when the logic wasn't handled
	 * @param switchValue - the value for the switch statement
	 * @throws LogicException
	 */
	void throwSwitchNotHandledException(String switchValue) {
		throwException(LogicException.class,
				   'core.logic.switchCaseUnhandled',
					[switchValue],
				   "The programming logic encounted an unexpected switch value of ${switchValue}"
		)
	}

	/**
	 * Retrieve the timezone object corresponding to the given code or the default otherwise.
	 * @param timezoneValue
	 * @param defaultTimeZoneCode
	 * @return
	 */
	Timezone getTimezone(String timezoneValue, String defaultTimeZoneCode = TimeUtil.defaultTimeZone) {
		Timezone timezone
		if (!StringUtil.isBlank(timezoneValue)) {
			timezone = Timezone.findByCode(timezoneValue)
		}
		if (!timezone) {
			timezone = Timezone.findByCode(TimeUtil.defaultTimeZone)
		}

		return timezone
	}

	/**
	 * Fetch the corresponding instance from the database (if the id is given). Otherwise, create a new one
	 * and set the project field (if needed).
	 *
	 * @param type - domain class.
	 * @param id - id of the instance (if null a new one will be created).
	 * @param currentProject - the user's current project.
	 * @param throwException - whether or not an exception should be thrown on error.
	 * @return
	 */
	@CompileStatic
	<T> T getOrCreate(Class<T> type, Object id, Project currentProject, boolean throwException = true) {
		T instance
		if (id != null) {
			instance = get(type, id, currentProject, throwException)
		} else {
			instance = type.newInstance()
			if (instance.hasProperty('project')) {
				instance['project'] = currentProject
			}
		}
		return instance
	}
}
