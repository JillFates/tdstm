package net.transitionmanager.controller

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.lang.CollectionUtils
import grails.converters.JSON
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.Errors

import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNAUTHORIZED

/**
 * Utility class for creating HTTP responses
 */
class ServiceResults {

	static void respondWithSuccess(HttpServletResponse response, Map data = [:]) {
		respondAsJson(response, success(data))
	}

	static void respondWithFailure(HttpServletResponse response, Map data = [:]) {
		respondAsJson(response, fail(data))
	}

	static void respondWithError(HttpServletResponse response, errorMsgs) {
		respondAsJson(response, errors(errorMsgs))
	}

	static Map success(Map data = [:]) {
		[status: 'success', data: data]
	}

	// TODO - JPM 5/2015 - I think that the fail() and respondWithFailure should? return list instead of map?
	static Map fail(Map data = [:]) {
		[status: 'fail', data: data]
	}

	static Map errors(errorStringOrList) {
		[status: 'error', errors: CollectionUtils.asList(errorStringOrList)]
	}

	static Map warnings(warnStringOrList) {
		[status: 'warning', warnings: CollectionUtils.asList(warnStringOrList)]
	}

	static void respondWithWarning(HttpServletResponse response, warningMsgs) {
		respondAsJson(response, warnings(warningMsgs))
	}

	static void respondAsJson(HttpServletResponse response, object = [:]) {
		response.setStatus(OK.value())
		setContentTypeJson(response)
		response.outputStream << (object as JSON)
	}

	/**
	 * Sends the text of a file as JSON to the response.
	 * @param response - the servlet response
	 * @param file - a text file
	 */
	static void respondAsJson(HttpServletResponse response, File file) {
		response.setStatus(OK.value())
		setContentTypeJson(response)
		response.outputStream << file.text
		response.flushBuffer()
	}

	static void unauthorized(HttpServletResponse response) {
		response.sendError(UNAUTHORIZED.value(), UNAUTHORIZED.reasonPhrase) // 401
	}

	static void methodFailure(HttpServletResponse response) {
		response.sendError(424, 'Method Failure')
	}

	static void internalError(HttpServletResponse response, log, Exception e) {
		log.error(e.message)
		response.addHeader('errorMessage', e.message)
		response.sendError(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR.reasonPhrase) // 500
	}

	static Map errorsInValidation(Errors errors) {
		MessageSource messageSource = ApplicationContextHolder.getBean('messageSource', MessageSource)
		def allErrorsAsArray = errors.allErrors.collect { messageSource.getMessage(it, LocaleContextHolder.locale) }
	}

	static Map invalidParams(errs) {
		errors(CollectionUtils.asList(errs))
	}

	static void forbidden(HttpServletResponse response, Exception e = null) {
		if (e) {
			response.addHeader('errorMessage', e.message)
		}
		response.sendError(FORBIDDEN.value(), FORBIDDEN.reasonPhrase) // 403, 'Forbidden'
	}

	static void notFound(HttpServletResponse response) {
		response.sendError(NOT_FOUND.value(), NOT_FOUND.reasonPhrase) // 404
	}

	static void setContentTypeJson(HttpServletResponse response) {
		response.setContentType('text/json')
	}
}
