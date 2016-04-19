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
	 * Used to respond to a request with a success message
	 * @param response - the HTTP response object
	 * @param map - a Map of any returning data (optional)
	 * @return void
	 */
	static void respondWithSuccess(response, map = [:]) {
		respondAsJson(response, success(map))
	}	

	/**
	 * Used to respond to a request with a Failure message
	 * @param response - the HTTP response object
	 * @param map - a Map of any returning data (optional)
	 * @return void
	 */
	static void respondWithFailure(response, map = [:]) {
		respondAsJson(response, fail(map))
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
	
	/**
	 * Used to respond to the browser via the servlet response by returning an object formatted as JSON
	 * @param response - the servlet response object
	 * @param object - the object to be rendered as JSON 
	 */
	static respondAsJson(response, object=[:]) {
		response.setStatus(200)
		setContentTypeJson(response)
		response.outputStream << ( object as JSON )
	}

	/**
	 * Used to respond to the browser via the servlet response by returning an object formatted as JSON
	 * @param response - the servlet response object
	 * @param object - the object to be rendered as JSON 
	 */
	static respondAsJson(response, File file) {
		response.setStatus(200)
		setContentTypeJson(response)
		//def fis = file.newInputStream()

		//response.outputStream << fis
		//fis.close()
		//response.flush()
		String json = file.text
		println "json=$json"
		response.outputStream << json
		response.flushBuffer()
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

	/**
	 * Used to set the ContentType to JSON
	 */
	static void setContentTypeJson(response) {
		response.setContentType('text/json')
	}
}
