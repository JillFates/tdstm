// import java.beans.StaticFieldsPersistenceDelegate;
import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.converters.JSON

/**
 * Utility class for creating HTTP resposes
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class ServiceResults {

	/**
	 * Returns a success response to be serialized as json
	 * @param map the Map data to be added to the response object
	 * @return the response map
	 */
	static Map success(map = [:]) {
		def renderMap = [:]
		renderMap.status = 'success'
		renderMap.data = map
		
		return renderMap
	}  

	/**
	 * Used to respond to a request with a success message
	 * @param response - the HTTP response object
	 * @param map - a Map of any returning data (optional)
	 * @return void
	 */
	static void respondWithSuccess(response, map = [:]) {
		response.outputStream << (success(map) as JSON)
	}	

// TODO - JPM 5/2015 - I think that the fail() and respondWithFailure should? return list instead of map?

	/**
	 * Returns a fail response to be serialized as json
	 * @param map the Map data to be added to the response object
	 * @return the response map
	 */
	static def fail(map = [:]) {
		def renderMap = [:]
		renderMap.status = 'fail'
		renderMap.data = map
		
		return renderMap
	}

	/**
	 * Used to respond to a request with a Failure message
	 * @param response - the HTTP response object
	 * @param map - a Map of any returning data (optional)
	 * @return void
	 */
	static void respondWithFailure(response, map = [:]) {
		response.outputStream << (fail(map) as JSON)
	}	
	
	/**
	 * Returns a error response to be serialized as json
	 * @param object an array or map to be set as errors
	 * @return the response map
	 */
	static Map errors(errorStringOrList) {
		def renderMap = [:]

		renderMap.status = 'error'
		if (errorStringOrList instanceof List) {
			renderMap.errors = errorStringOrList
		} else {
			renderMap.errors = [ errorStringOrList ]
		}
		
		return renderMap
	}
	
	/**
	 * Used to respond to a request with a Failure message
	 * @param response - the HTTP response object
	 * @param An error message string or list of error messages
	 * @return void
	 */
	static void respondWithError(response, errorMsgs) {
		respondAsJson(response, errors(errorMsgs))
	}	

	/**
	 * Returns a warning response to be serialized as json
	 * @param object an array or map to be set as warnings
	 * @return the response map
	 */
	static def warnings(warnStringOrList) {
		def renderMap = [:]
		renderMap.status = 'warning'
		if (warnStringOrList instanceof List) {
			renderMap.errors = warnStringOrList
		} else {
			renderMap.warnings = [ warnStringOrList ]
		}
		println "warnings = $renderMap"
		return renderMap
	}

	/**
	 * Used to respond to a request with a Failure message
	 * @param response - the HTTP response object
	 * @param An error message string or list of error messages
	 * @return void
	 */
	static void respondWithWarning(response, warningMsgs) {
		respondAsJson(response, warnings(warningMsgs))
	}	
	
	static respondAsJson(response, object) {
		if (! object)
			object = [:]
		response.setHeader('content-type', 'application/json')
		response.outputStream << ( object as JSON )
	}

	/**
	 * Sends an unauthorized error
	 * @param response the response object
	 */
	static def unauthorized(response) {
		response.sendError(401, 'Unauthorized error')
	}
	
	/**
	 * Sends a method failure error
	 * @param response the response object
	 */
	static def methodFailure(response) {
		response.sendError(424, 'Method Failure')
	}
	
	/**
	 * Internal error
	 * @param response the response object
	 */
	static def internalError(response, log, Exception e) {
		log.error(e.getMessage())
		response.addHeader("errorMessage", e.getMessage())
		response.sendError(500, 'Internal server error')
	}
	
	/**
	 * Sends a method failure error with the validation errors
	 * @param response the response object
	 */
	static def errorsInValidation(errs) {
		def messageSource = ApplicationHolder.application.mainContext.messageSource
		def locale = null
		def allErrorsAsArray = errs.allErrors.collect { it -> "${messageSource.getMessage(it, locale)}" }
		return errors(allErrorsAsArray)
	}

	/**
	 * Sends a method failure error with the validation errors
	 * @param response the response object
	 */
	static Map invalidParams(errs) {
		if (errs instanceof String)
			errs = [errs]
		return errors(errs)
	}

	
	/**
	 * Sends a forbidden error
	 * @param response the response object
	 */
	static void forbidden(response, log = null, Exception e = null) {
		if (e != null) {
			response.addHeader("errorMessage", e.getMessage())
		}
		response.sendError(403, 'Forbidden')
	}

	/**
	 * Sends a Not Found 404 error
	 * @param response the response object
	 */
	static void notFound(response) {
		response.sendError(404, 'Not Found')
	}	

}
