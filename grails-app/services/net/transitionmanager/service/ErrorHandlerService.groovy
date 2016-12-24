package net.transitionmanager.service

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
	 * Used to determine if a request was issued by a Javascript AJAX call
	 * @param request - the HttpRequest object
	 * @return true if the request is from an Ajax client
	 */
	boolean isAjaxRequest(request) {
		boolean isAjax = 'XMLHttpRequest'.equals( request.getHeader('X-Requested-With') )
		if (! isAjax) {
			// Angular in particular doesn't set the X-Requested-With header so we check for Accept allowing json
			String accept = request.getHeader('Accept')
			if (accept) {
				List l = accept.split(',')
				isAjax = l.contains('application/json')
			}
		}
		return isAjax
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

	Exception getException(request) {
		request.getAttribute('exception')
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
			if (exception instanceof org.codehaus.groovy.grails.web.errors.GrailsWrappedRuntimeException) {
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