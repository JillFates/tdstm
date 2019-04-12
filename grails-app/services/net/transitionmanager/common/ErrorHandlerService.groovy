package net.transitionmanager.common

import org.grails.web.errors.GrailsWrappedRuntimeException
import org.springframework.web.util.UrlPathHelper

/**
 * The ErrorHandler service provides some functionality that is used by the page error handler
 * logic for the views/errorHandler pages.
 */
class ErrorHandlerService {

	static transactional = false

	CoreService coreService
	/**
	 * Gets the exception name that caused the error
	 * @param ex - the org.codehaus.groovy.grails.web.errors.GrailsWrappedRuntimeException that is exposed to the page
	 */
	String exceptionName(RuntimeException ex) {
		List parts = ex.stackTraceLines[0].split(':')
		String name = parts ? parts[0] : 'Unknown'
		name
	}

	/**
	 * Gets the exception name that caused the error
	 * @param ex - the org.codehaus.groovy.grails.web.errors.GrailsWrappedRuntimeException that is exposed to the page
	 */
	String shortExceptionName(RuntimeException ex) {
		String name = exceptionName(ex)

		if (name.contains('.')) {
			name=name.split('\\.')[-1]
		}

		name
	}

	/**
	 * Used to determine if the stacktrace should be displayed in the error response page
	 */
	boolean showStacktrace() {
		return (coreService.getEnvironment().toLowerCase() == 'development')
	}

	/**
	 * Used to determine if the content requested is static content vs dynamic pages content
	 * @param request - the HttpRequest object
	 * @return true if the request is for some sort of static content (e.g. javascript, css or images)
	 */
	boolean isStaticContent(request) {
		boolean r = false
		String uri = requestUri(request)
		if (uri.contains('.')) {
			r = true
		}

		// log.debug "isStaticContent() uri=$uri, isStatic? $r"
		return r
	}

	/**
	 * Used to extract the URI of the request
	 * @param request - the HttpRequest to get the URI from
	 * @return the URI of the request
	 */
	String requestUri(request) {
		UrlPathHelper helper = new UrlPathHelper()
		String uri = helper.getOriginatingRequestUri(request)
		return uri
	}

	/** 
	 * Used to retrieve the exception that was caught at the controller layer
	 * @return the exception that occurred
	 */
	Exception getException(request) {
		request.getAttribute('exception')
	}

	/**
	 * Used to set the exception that the controller error handler layer should report
	 * @param ex - the exception to report
	 */
	void setException(Exception ex, request) {
		request.setAttribute('exception', ex)
	}

	/**
	 * Used internally to build up the default model used by page responses
	 * @return A map consisting of the following attributes:
	 *	  continueUrl - the URL to continue to after showing an error, forbidden or not found messages
	 *	  showStacktrace - a flag used to determine if the stacktrace should appear in the error page
	 * 	  exception - the unwrapped Exception that resulted in the error
	 *	  exceptionMsg - the error message of the unwrapped Exception
	 */
	Map model(request) {
		def exception = getException(request)
		if (exception) {
			if (exception instanceof GrailsWrappedRuntimeException) {
				// Get to the real exception wrapped in the Grails exception
				exception = exception.cause
				log.debug "model() in GrailsWrappedRuntimeException scenario"
			} else {
				log.debug "model() exception=${exception.getClass().getName()}"
			}
		}

		[
			continueUrl: coreService.getApplicationUrl(),
			showStacktrace: showStacktrace(),
			exception: exception,
			exceptionMsg: exception?.getMessage(),
			requestUri: requestUri(request)
		]
	}

}