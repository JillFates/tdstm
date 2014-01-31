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
	static def success(map) {
		def renderMap = [:]
		renderMap.status = 'success'
		renderMap.data = map
		
		return renderMap
	}  
}
