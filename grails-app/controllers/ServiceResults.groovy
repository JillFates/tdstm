// import java.beans.StaticFieldsPersistenceDelegate;
import org.codehaus.groovy.grails.commons.ApplicationHolder


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
	static def success(map = [:]) {
		def renderMap = [:]
		renderMap.status = 'success'
		renderMap.data = map
		
		return renderMap
	}  
	
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
	static def errors(object) {
		def renderMap = [:]
		renderMap.status = 'error'
		renderMap.errors = object ?: []
		
		return renderMap
	}
	
	/**
	 * Returns a warning response to be serialized as json
	 * @param object an array or map to be set as warnings
	 * @return the response map
	 */
	static def warnings(object) {
		def renderMap = [:]
		renderMap.status = 'warning'
		renderMap.warnings = object ?: []
		
		return renderMap
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
	 * Sends a forbidden error
	 * @param response the response object
	 */
	static def forbidden(response) {
		response.sendError(403, 'Forbidden')
	}
}
