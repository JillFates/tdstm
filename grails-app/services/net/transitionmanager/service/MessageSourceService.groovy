package net.transitionmanager.service

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

class MessageSourceService {

	MessageSource messageSource

	String getI18NMessage(String code) {
		return getI18NMessage(code, [] as Object[], '')
	}

	String getI18NMessage(String code, String defaultMessage) {
		return getI18NMessage(code, [] as Object[], defaultMessage)
	}

	String getI18NMessage(String code, Object[] args) {
		return getI18NMessage(code, args, null)
	}

	String getI18NMessage(String code, Object[] args, String defaultMessage, Locale locale = LocaleContextHolder.locale) {
		return messageSource.getMessage(code, args, defaultMessage, locale)
	}

}
