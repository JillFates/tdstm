package net.transitionmanager.common

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.ObjectError

/**
 * This service wraps the access to the MessageSource class for i18n messages.
 * The idea of having this wrapper service is that we can inject it into another beans
 * created within the resources.groovy file
 * e.g. TaskFacade
 */
class MessageSourceService {

	MessageSource messageSource

	/**
	 * Get an i18n message
	 * @param code - message code
	 * @return
	 */
	String i18nMessage(String code) {
		return i18nMessage(code, [] as Object[], '')
	}

	/**
	 * Get an i18n message
	 * @param code - message code
	 * @param defaultMessage - default message if message code is not found
	 * @return
	 */
	String i18nMessage(String code, String defaultMessage) {
		return i18nMessage(code, [] as Object[], defaultMessage)
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
		return messageSource.getMessage(code, args, defaultMessage, locale)
	}

	/**
	 * Return the corresponding error message for the ObjectError and Locale received.
	 * @param objectError
	 * @param locale
	 * @return
	 */
	String i18nMessage(ObjectError objectError, Locale locale = LocaleContextHolder.locale){
		return messageSource.getMessage(objectError, locale)
	}

}
