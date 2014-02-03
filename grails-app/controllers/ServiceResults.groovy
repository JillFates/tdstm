import java.beans.StaticFieldsPersistenceDelegate;


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
	 * Sends a forbidden error
	 * @param response the response object
	 */
	static def forbidden(response) {
		response.sendError(403, 'Forbidden')
	}
}
